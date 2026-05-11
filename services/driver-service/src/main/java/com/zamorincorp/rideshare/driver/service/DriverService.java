package com.zamorincorp.rideshare.driver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.zamorincorp.rideshare.driver.dto.RideDispatchEvent;
import com.zamorincorp.rideshare.driver.entity.Driver;
import com.zamorincorp.rideshare.driver.repository.DriverRepository;
import com.zamorincorp.rideshare.driver.entity.DriverStatus;
import com.zamorincorp.rideshare.driver.dto.RideAcceptedEvent;
import java.time.LocalDateTime;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import com.zamorincorp.rideshare.driver.dto.UserCreatedEvent;

@Service
@Slf4j
public class DriverService {

    @Autowired
    private DriverRepository driverRepository;
    

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.kafka.topics.ride-accepted}")
    private String rideAcceptedTopic;

    @Value("${app.kafka.events.ride-accepted.type}")
    private String rideAcceptedEventType;

    @Value("${app.kafka.events.ride-accepted.version}")
    private String rideAcceptedEventVersion;

    public void handleDriverAssigned(RideDispatchEvent event) {
        log.info("Handling driver assigned event: tripId={} riderId={} driverId={}", event.tripId(), event.riderId(), event.driverId());

        // 1. Find driver in DB
        Driver driver = driverRepository.findByDriverId(event.driverId()).orElseThrow(() -> new RuntimeException("Driver not found"));
    
        // 2. Set status to BUSY
        driver.setStatus(DriverStatus.BUSY);

        // 3. Persist change
        driverRepository.save(driver);
        log.info("Driver status updated to BUSY for driverId={}", event.driverId());
        
        // 4. Produce a 'TripAccepted' event (to let Rider Service know)
        RideAcceptedEvent rideAcceptedEvent = new RideAcceptedEvent(
            event.tripId(),
            event.riderId(),
            event.driverId(),
            event.pickupLocation(),
            event.destination(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        String payload;
        try {
            payload = objectMapper.writeValueAsString(rideAcceptedEvent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize ride accepted event", e);
        }
        Message<String> eventMessage = MessageBuilder.withPayload(payload)
        .setHeader(KafkaHeaders.TOPIC, rideAcceptedTopic)
        .setHeader(KafkaHeaders.KEY, rideAcceptedEvent.tripId().toString())
        .setHeader("eventType", rideAcceptedEventType)
        .setHeader("eventVersion", rideAcceptedEventVersion)
        .build();
        kafkaTemplate.send(eventMessage);
        log.info("Ride accepted event sent for tripId={} riderId={} driverId={}", event.tripId(), event.riderId(), event.driverId());
    }

    public void handleUserCreated(UserCreatedEvent event) {
        log.info("Handling user created event: userId={} username={} role={}", event.userId(), event.username(), event.role());
        
        // 1. Find driver in DB if not found, create a new driver
        Driver driver = driverRepository.findByDriverId(event.userId())
        .orElseGet(() -> driverRepository.save(
            Driver.builder()
                .driverId(event.userId())
                .name(event.username()) // from UserCreatedEvent
                .status(DriverStatus.ONLINE) // or OFFLINE, your default choice
                .currentVehicle(null)
                .build()
        ));

        // 2. Set status to AVAILABLE
        // driver.setStatus(DriverStatus.AVAILABLE);

        // 3. Persist change
        // driverRepository.save(driver);
        log.info("Driver status updated to AVAILABLE for driverId={}", event.userId());
    }
}
