package foo.hari.rideshare.driver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foo.hari.rideshare.driver.dto.RideAcceptedEvent;
import foo.hari.rideshare.driver.dto.RideDispatchEvent;
import foo.hari.rideshare.driver.dto.UserCreatedEvent;
import foo.hari.rideshare.driver.entity.Driver;
import foo.hari.rideshare.driver.entity.DriverStatus;
import foo.hari.rideshare.driver.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    private static final UUID DRIVER_ID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440009");
    private static final UUID EXISTING_USER_ID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID NEW_USER_ID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440002");

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DriverService driverService;

    @Captor
    private ArgumentCaptor<Driver> driverCaptor;

    @Captor
    private ArgumentCaptor<Message<String>> messageCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(driverService, "rideAcceptedTopic", "ride-accepted.v1");
        ReflectionTestUtils.setField(driverService, "rideAcceptedEventType",
                "foo.hari.rideshare.driver.dto.RideAcceptedEvent");
        ReflectionTestUtils.setField(driverService, "rideAcceptedEventVersion", "1.0.0");
    }

    @Test
    void handleDriverAssigned_marksDriverBusyAndPublishesEvent() throws Exception {
        RideDispatchEvent event = sampleDispatchEvent();
        Driver driver = Driver.builder()
                .driverId(DRIVER_ID)
                .name("Bob")
                .status(DriverStatus.ONLINE)
                .build();
        when(driverRepository.findById(DRIVER_ID)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(objectMapper.writeValueAsString(any(RideAcceptedEvent.class))).thenReturn("{\"ok\":true}");

        driverService.handleDriverAssigned(event);

        verify(driverRepository).save(driverCaptor.capture());
        assertEquals(DriverStatus.BUSY, driverCaptor.getValue().getStatus());

        verify(kafkaTemplate).send(messageCaptor.capture());
        Message<String> message = messageCaptor.getValue();
        assertEquals("{\"ok\":true}", message.getPayload());
        assertEquals("ride-accepted.v1", message.getHeaders().get(KafkaHeaders.TOPIC));
        assertEquals("42", message.getHeaders().get(KafkaHeaders.KEY));
        assertEquals("foo.hari.rideshare.driver.dto.RideAcceptedEvent",
                message.getHeaders().get("eventType"));
        assertEquals("1.0.0", message.getHeaders().get("eventVersion"));
    }

    @Test
    void handleDriverAssigned_whenDriverNotFound_throwsRuntimeException() {
        RideDispatchEvent event = sampleDispatchEvent();
        when(driverRepository.findById(DRIVER_ID)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> driverService.handleDriverAssigned(event));

        verify(driverRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(Message.class));
    }

    @Test
    void handleDriverAssigned_whenSerializationFails_throwsIllegalStateException() throws Exception {
        RideDispatchEvent event = sampleDispatchEvent();
        Driver driver = Driver.builder()
                .driverId(DRIVER_ID)
                .status(DriverStatus.ONLINE)
                .build();
        when(driverRepository.findById(DRIVER_ID)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new JsonProcessingException("boom") { })
                .when(objectMapper).writeValueAsString(any(RideAcceptedEvent.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> driverService.handleDriverAssigned(event));
        assertTrue(ex.getMessage().contains("Could not serialize ride accepted event"));
        verify(kafkaTemplate, never()).send(any(Message.class));
    }

    @Test
    void handleUserCreated_whenDriverExists_doesNotCreateNewDriver() {
        UserCreatedEvent event = sampleUserCreatedEvent(EXISTING_USER_ID);
        Driver existing = Driver.builder()
                .driverId(EXISTING_USER_ID)
                .name("existing")
                .status(DriverStatus.BUSY)
                .build();
        when(driverRepository.findById(EXISTING_USER_ID)).thenReturn(Optional.of(existing));

        driverService.handleUserCreated(event);

        verify(driverRepository, never()).save(any());
    }

    @Test
    void handleUserCreated_whenDriverMissing_createsOnlineDriver() {
        UserCreatedEvent event = sampleUserCreatedEvent(NEW_USER_ID);
        when(driverRepository.findById(NEW_USER_ID)).thenReturn(Optional.empty());
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        driverService.handleUserCreated(event);

        verify(driverRepository).save(driverCaptor.capture());
        Driver created = driverCaptor.getValue();
        assertEquals(NEW_USER_ID, created.getDriverId());
        assertEquals("alice", created.getName());
        assertEquals(DriverStatus.ONLINE, created.getStatus());
    }

    private RideDispatchEvent sampleDispatchEvent() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 12, 0);
        return new RideDispatchEvent(
                42L,
                "rider-1",
                DRIVER_ID.toString(),
                "-79.38,43.65",
                "-79.40,43.70",
                now,
                now
        );
    }

    private UserCreatedEvent sampleUserCreatedEvent(UUID userId) {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 12, 0);
        return new UserCreatedEvent(userId.toString(), "alice", "DRIVER", now, now);
    }
}
