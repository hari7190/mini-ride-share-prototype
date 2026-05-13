package com.zamorincorp.rideshare.dispatch.service;

import com.zamorincorp.rideshare.dispatch.dto.RideRequestedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.zamorincorp.rideshare.dispatch.entity.RideDispatch;
import com.zamorincorp.rideshare.dispatch.entity.DispatchStatus;
import com.zamorincorp.rideshare.dispatch.repository.RideDispatchRepository;
import com.zamorincorp.rideshare.dispatch.repository.DriverLocationQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import com.zamorincorp.rideshare.dispatch.dto.RideDispatchEvent;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.beans.factory.annotation.Value;

@Service
@Slf4j
public class DispatchService {
    @Autowired
    private RideDispatchRepository rideDispatchRepository;

    @Autowired
    private DriverLocationQueryRepository driverLocationQueryRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.kafka.topics.ride-dispatch}")
    private String rideRequestsTopic;

    @Value("${app.kafka.events.ride-dispatch.type}")
    private String rideDispatchEventType;

    @Value("${app.kafka.events.ride-dispatch.version}")
    private String rideDispatchEventVersion;

    public void handleRideRequested(RideRequestedEvent event) {
        log.info(
            "Dispatch received ride request: tripId={} riderId={} pickup={} destination={} type={} version={}",
            event.tripId(),
            event.riderId(),
            event.pickupLocation(),
            event.destination(),
            event.eventType(),
            event.eventVersion());

        // 1. Create a new ride dispatch record
        RideDispatch rideDispatch = new RideDispatch();
        rideDispatch.setTripId(event.tripId());
        rideDispatch.setRiderId(event.riderId());
        rideDispatch.setStatus(DispatchStatus.SEARCHING);
            
        rideDispatchRepository.save(rideDispatch);
        log.info("Ride dispatch created for tripId={} riderId={}", event.tripId(), event.riderId());
        
        // Mock matching logic
        String driverId = findMatchingDriver(event.pickupLocation(), event.destination());

        // 2. Update the ride dispatch record with the driver id
        if (driverId != null) {
            rideDispatch.setDriverId(driverId);
            rideDispatch.setStatus(DispatchStatus.MATCHED);
            RideDispatch saved = rideDispatchRepository.save(rideDispatch);

            log.info("Ride dispatch updated for tripId={} riderId={} driverId={}", event.tripId(), event.riderId(), driverId);

            // 3. Publish a ride dispatch event
            publishDriverAssignedEvent(event.tripId(), event.riderId(), driverId, event.pickupLocation(), event.destination(), saved);

            log.info("Ride dispatch event published for tripId={} riderId={} driverId={}", event.tripId(), event.riderId(), driverId);
        } else {
            rideDispatch.setStatus(DispatchStatus.FAILED);
            rideDispatchRepository.save(rideDispatch);
            log.info("No matching driver found for tripId={} riderId={}", event.tripId(), event.riderId());
        }
    }

    private String findMatchingDriver(String pickupLocation, String destination) {
        log.debug("Finding nearest driver to pickup={} destination={}", pickupLocation, destination);
        double longitude;
        double latitude;
        try {
            String[] parts = pickupLocation.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("expected \"longitude,latitude\"");
            }
            longitude = Double.parseDouble(parts[0].trim());
            latitude = Double.parseDouble(parts[1].trim());
        } catch (RuntimeException e) {
            log.warn("Invalid pickupLocation={}, cannot match drivers", pickupLocation, e);
            return null;
        }

        return driverLocationQueryRepository
            .findNearestDriverId(longitude, latitude)
            .orElse(null);
    }

    // TODO: Add a retry mechanism for the Kafka send
    private void publishDriverAssignedEvent(Long tripId, String riderId, String driverId, String pickupLocation, String destination, RideDispatch saved) {

        RideDispatchEvent rideDispatchEvent = new RideDispatchEvent(
            tripId,
            riderId,
            driverId,
            pickupLocation, //So that driver-service dont' have to call back immediately.
            destination,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(rideDispatchEvent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize ride dispatch event", e);
        }

        try {
        Message<String> eventMessage = MessageBuilder.withPayload(payload)
        .setHeader(KafkaHeaders.TOPIC, rideRequestsTopic)
        .setHeader(KafkaHeaders.KEY, saved.getId().toString())
        .setHeader("eventType", rideDispatchEventType)
        .setHeader("eventVersion", rideDispatchEventVersion)
        .build();

        kafkaTemplate.send(eventMessage);
        } catch (KafkaException e) {
            log.error("Error sending ride dispatch event for tripId={} riderId={} driverId={}", tripId, riderId, driverId, e);
            throw new IllegalStateException("Could not send ride dispatch event", e);
        }
    }

}
