package com.zamorincorp.rideshare.driver.consumer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamorincorp.rideshare.driver.dto.RideDispatchEvent;
import com.zamorincorp.rideshare.driver.dto.UserCreatedEvent;
import com.zamorincorp.rideshare.driver.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DispatchKafkaListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DriverService driverService;

    @InjectMocks
    private DispatchKafkaListener dispatchKafkaListener;

    private ObjectMapper realObjectMapper;

    @BeforeEach
    void setUp() {
        realObjectMapper = new ObjectMapper();
        realObjectMapper.findAndRegisterModules();
    }

    @Test
    void onDriverAssigned_delegatesToDriverService() throws Exception {
        String payload = realObjectMapper.writeValueAsString(sampleDispatchEvent());
        RideDispatchEvent event = sampleDispatchEvent();
        when(objectMapper.readValue(payload, RideDispatchEvent.class)).thenReturn(event);

        dispatchKafkaListener.onDriverAssigned(payload);

        verify(driverService).handleDriverAssigned(event);
    }

    @Test
    void onDriverAssigned_whenPayloadInvalid_doesNotDelegate() throws Exception {
        String payload = "not-json";
        when(objectMapper.readValue(payload, RideDispatchEvent.class))
                .thenThrow(new JsonParseException(null, "bad json"));

        dispatchKafkaListener.onDriverAssigned(payload);

        verify(driverService, never()).handleDriverAssigned(any());
    }

    @Test
    void onUserCreated_delegatesToDriverService() throws Exception {
        String payload = realObjectMapper.writeValueAsString(sampleUserCreatedEvent());
        UserCreatedEvent event = sampleUserCreatedEvent();
        when(objectMapper.readValue(payload, UserCreatedEvent.class)).thenReturn(event);

        dispatchKafkaListener.onUserCreated(payload);

        verify(driverService).handleUserCreated(event);
    }

    @Test
    void onUserCreated_whenPayloadInvalid_doesNotDelegate() throws Exception {
        String payload = "not-json";
        when(objectMapper.readValue(payload, UserCreatedEvent.class))
                .thenThrow(new JsonParseException(null, "bad json"));

        dispatchKafkaListener.onUserCreated(payload);

        verify(driverService, never()).handleUserCreated(any());
    }

    private RideDispatchEvent sampleDispatchEvent() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 12, 0);
        return new RideDispatchEvent(
                42L,
                "rider-1",
                "driver-9",
                "-79.38,43.65",
                "-79.40,43.70",
                now,
                now
        );
    }

    private UserCreatedEvent sampleUserCreatedEvent() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 12, 0);
        return new UserCreatedEvent("driver-1", "alice", "DRIVER", now, now);
    }
}
