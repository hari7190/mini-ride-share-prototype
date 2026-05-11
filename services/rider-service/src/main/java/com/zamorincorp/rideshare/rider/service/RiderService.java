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
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

@Service
@Slf4j
public class RiderService {
    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

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

        log.info("Creating ride request for riderId={} pickupLocation={} destination={}"
        , riderId, rideRequestDTO.getPickupLocation(), rideRequestDTO.getDestination());

        // 1. Convert DTO to Model (Mapping)
        Trip trip = new Trip();
        trip.setRiderId(riderId);
        trip.setPickupLocation(convertToPoint(rideRequestDTO.getPickupLocation()));
        trip.setDestination(convertToPoint(rideRequestDTO.getDestination()));
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
                saved.getPickupLocation().toString(),
                saved.getDestination().toString(),
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
