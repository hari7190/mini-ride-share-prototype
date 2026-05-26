package com.zamorincorp.rideshare.rider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamorincorp.rideshare.rider.dto.RideRequestedEvent;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.zamorincorp.rideshare.rider.entity.Ride;
import com.zamorincorp.rideshare.rider.dto.RideRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import com.zamorincorp.rideshare.rider.repository.RideRepository;
import com.zamorincorp.rideshare.rider.entity.RideStatus;

@Service
@Slf4j
public class RiderService {
    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired
    private RideRepository rideRepository;

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
    public Ride createRideRequest(RideRequestDTO rideRequestDTO, String riderIdSubject) {
        UUID riderId = UUID.fromString(riderIdSubject);

        log.info("Creating ride request for riderId={} pickupLocation={} destination={}"
        , riderId, rideRequestDTO.pickupLocation(), rideRequestDTO.destination());

        // 1. Convert DTO to Model (Mapping)
        Ride trip = new Ride();
        trip.setRiderId(riderId);
        trip.setPickupLocation(convertToPoint(rideRequestDTO.pickupLocation()));
        trip.setDestination(convertToPoint(rideRequestDTO.destination()));
        trip.setStatus(RideStatus.PENDING);

        // 2. Save the trip to the database
        Ride saved = rideRepository.save(trip);

        // 3. Publish a structured, versioned event for downstream matching services
        RideRequestedEvent rideRequestedEvent = new RideRequestedEvent(
                UUID.randomUUID().toString(),
                rideRequestedEventType,
                rideRequestedEventVersion,
                Instant.now().toString(),
                saved.getId(),
                saved.getRiderId().toString(),
                saved.getPickupLocation().getCoordinate().x + ", " + saved.getPickupLocation().getCoordinate().y,
                saved.getDestination().getCoordinate().x + ", " + saved.getDestination().getCoordinate().y,
                saved.getStatus().toString()
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

    private Point convertToPoint(String location) {
        String[] parts = location.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("currentLocation must be \"longitude,latitude\"");
        }
        double longitude = Double.parseDouble(parts[0].trim());
        double latitude = Double.parseDouble(parts[1].trim());
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }
}
