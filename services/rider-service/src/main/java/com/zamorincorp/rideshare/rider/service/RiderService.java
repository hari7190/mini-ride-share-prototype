package com.zamorincorp.rideshare.rider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamorincorp.rideshare.rider.dto.RideRequestedEvent;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.zamorincorp.rideshare.rider.repository.TripRepository;
import com.zamorincorp.rideshare.rider.entity.Trip;
import com.zamorincorp.rideshare.rider.dto.RideRequestDTO;
import com.zamorincorp.rideshare.rider.entity.TripStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@Service
public class RiderService {
    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.kafka.topics.ride-requests}")
    private String rideRequestsTopic;

    @Value("${app.kafka.events.ride-requested.type}")
    private String rideRequestedEventType;

    @Value("${app.kafka.events.ride-requested.version}")
    private String rideRequestedEventVersion;

    //create createRideRequest method
    public Trip createRideRequest(RideRequestDTO rideRequestDTO, String riderId) {
        // 1. Convert DTO to Model (Mapping)
        Trip trip = new Trip();
        trip.setRiderId(riderId);
        trip.setPickupLocation(rideRequestDTO.getPickupLocation());
        trip.setDestination(rideRequestDTO.getDestination());
        trip.setStatus(TripStatus.PENDING);

        // 2. Save the trip to the database
        Trip saved = tripRepository.save(trip);

        // 3. Publish a structured, versioned event for downstream matching services
        RideRequestedEvent rideRequestedEvent = new RideRequestedEvent(
                UUID.randomUUID().toString(),
                rideRequestedEventType,
                rideRequestedEventVersion,
                Instant.now().toString(),
                saved.getId(),
                saved.getRiderId(),
                saved.getPickupLocation(),
                saved.getDestination(),
                saved.getStatus().name()
        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(rideRequestedEvent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize ride request event", e);
        }

        Message<String> eventMessage = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, rideRequestsTopic)
                .setHeader(KafkaHeaders.KEY, saved.getId().toString())
                .setHeader("eventType", rideRequestedEventType)
                .setHeader("eventVersion", rideRequestedEventVersion)
                .build();

        kafkaTemplate.send(eventMessage);

        return saved;
    }
}
