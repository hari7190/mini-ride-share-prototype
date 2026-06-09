package foo.hari.rideshare.driver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import foo.hari.rideshare.driver.dto.RideDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import foo.hari.rideshare.driver.dto.RideDispatchEvent;
import foo.hari.rideshare.driver.entity.Driver;
import foo.hari.rideshare.driver.repository.DriverRepository;
import foo.hari.rideshare.driver.entity.DriverStatus;
import foo.hari.rideshare.driver.entity.RideStatus;
import foo.hari.rideshare.driver.dto.RideAcceptedEvent;
import foo.hari.rideshare.driver.dto.RideDeclinedEvent;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import foo.hari.rideshare.driver.dto.UserCreatedEvent;

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

    @Value("${app.kafka.topics.driver-declined}")
    private String driverDeclinedTopic;

    @Value("${app.kafka.events.driver-declined.type}")
    private String driverDeclinedEventType;

    @Value("${app.kafka.events.driver-declined.version}")
    private String driverDeclinedEventVersion;

    // fetch rides
    public RideDTO fetchRide(String driverId) {
        return new RideDTO(
                driverRepository.findById(parseDriverId(driverId)).map(Driver::getCurrentTripId)
                        .orElseThrow(() -> new RuntimeException("Driver not found")),
                RideStatus.MATCHED);
    }

    // accept ride
    public String acceptRide(String driverId){
        return "ride-confirmed";
    }

    // decline ride
    public String declineRide(String driverId, RideDTO rideDTO){
        Driver driver = driverRepository.findById(parseDriverId(driverId))
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        if (rideDTO == null || rideDTO.rideId() == null) {
            throw new IllegalArgumentException("Ride payload is required");
        }

        driver.setStatus(DriverStatus.AVAILABLE);
        driver.setCurrentTripId(null);
        driverRepository.save(driver);
        publishDriverDeclinedEvent(driverId, rideDTO);

        return "decline-confirmed";
    }

    // ride finished
    public String rideFinish(String driverId){
        return "ride-finished";
    }

    // handle events
    public void handleDriverAssigned(RideDispatchEvent event) {
        log.info("Handling driver assigned event: tripId={} riderId={} driverId={}", event.tripId(), event.riderId(), event.driverId());

        // 1. Find driver in DB
        Driver driver = driverRepository.findById(parseDriverId(event.driverId()))
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // 2. Set status to BUSY
        driver.setStatus(DriverStatus.BUSY);

        // 3. Persist change
        driverRepository.save(driver);
        log.info("Driver status updated to BUSY for driverId={}", event.driverId());

        // 4. Produce a 'TripAccepted' event (to let Rider Service know)
        publishDriverAssignedEvent(event);

    }

    public void handleUserCreated(UserCreatedEvent event) {
        log.info("Handling user created event: userId={} username={} role={}", event.userId(), event.username(), event.role());

        // 1. Find driver in DB if not found, create a new driver
        UUID userId = parseDriverId(event.userId());
        driverRepository.findById(userId)
                .orElseGet(() -> driverRepository.save(
                        Driver.builder()
                                .driverId(userId)
                                .name(event.username()) // from UserCreatedEvent
                                .status(DriverStatus.ONLINE) // or OFFLINE, your default choice
                                .currentVehicle(null)
                                .build()
                ));

        log.info("Driver status updated to AVAILABLE for driverId={}", userId);
    }

    private static UUID parseDriverId(String driverId) {
        return UUID.fromString(driverId);
    }

    private void publishDriverAssignedEvent(RideDispatchEvent event) {
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

    private void publishDriverDeclinedEvent(String driverId, RideDTO rideDTO) {
        RideDeclinedEvent rideDeclinedEvent = new RideDeclinedEvent(
                rideDTO.rideId(),
                driverId,
                RideStatus.DECLINED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        String payload;
        try {
            payload = objectMapper.writeValueAsString(rideDeclinedEvent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize ride declined event", e);
        }
        Message<String> eventMessage = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, driverDeclinedTopic)
                .setHeader(KafkaHeaders.KEY, rideDeclinedEvent.tripId().toString())
                .setHeader("eventType", driverDeclinedEventType)
                .setHeader("eventVersion", driverDeclinedEventVersion)
                .build();
        kafkaTemplate.send(eventMessage);
        log.info("Driver declined event sent for tripId={} driverId={}", rideDeclinedEvent.tripId(), rideDeclinedEvent.driverId());
    }


}
