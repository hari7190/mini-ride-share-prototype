package com.zamorincorp.rideshare.rider.consumer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamorincorp.rideshare.rider.dto.DriverAssignedEventDTO;
import com.zamorincorp.rideshare.rider.entity.Ride;
import com.zamorincorp.rideshare.rider.entity.RideStatus;
import com.zamorincorp.rideshare.rider.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideUpdateHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RideRepository rideRepository;

    @Captor
    private ArgumentCaptor<Ride> tripCaptor;

    private RideUpdateHandler rideUpdateHandler;

    private ObjectMapper realObjectMapper;

    @BeforeEach
    void setUp() {
        rideUpdateHandler = new RideUpdateHandler(objectMapper);
        ReflectionTestUtils.setField(rideUpdateHandler, "rideRepository", rideRepository);
        realObjectMapper = new ObjectMapper();
        realObjectMapper.findAndRegisterModules();
    }

    @Test
    void onDriverAssigned_updatesTripStatusToAccepted() throws Exception {
        String payload = realObjectMapper.writeValueAsString(sampleEvent(99L));
        Ride trip = new Ride();
        trip.setId(99L);
        trip.setStatus(RideStatus.PENDING);
        when(objectMapper.readValue(payload, DriverAssignedEventDTO.class)).thenReturn(sampleEvent(99L));
        when(rideRepository.findById(99L)).thenReturn(Optional.of(trip));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        rideUpdateHandler.onDriverAssigned(payload);

        verify(rideRepository).save(tripCaptor.capture());
        assertEquals(RideStatus.MATCHED, tripCaptor.getValue().getStatus());
    }

    @Test
    void onDriverAssigned_whenTripNotFound_doesNotSave() throws Exception {
        String payload = realObjectMapper.writeValueAsString(sampleEvent(404L));
        when(objectMapper.readValue(payload, DriverAssignedEventDTO.class)).thenReturn(sampleEvent(404L));
        when(rideRepository.findById(404L)).thenReturn(Optional.empty());

        rideUpdateHandler.onDriverAssigned(payload);

        verify(rideRepository, never()).save(any());
    }

    @Test
    void onDriverAssigned_whenPayloadInvalid_doesNotSave() throws Exception {
        String payload = "not-json";
        when(objectMapper.readValue(payload, DriverAssignedEventDTO.class))
                .thenThrow(new JsonParseException(null, "bad json"));

        rideUpdateHandler.onDriverAssigned(payload);

        verify(rideRepository, never()).save(any());
    }

    private DriverAssignedEventDTO sampleEvent(Long tripId) {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 12, 0);
        return new DriverAssignedEventDTO(
                tripId,
                "rider-1",
                "driver-9",
                "-79.38,43.65",
                "-79.40,43.70",
                now,
                now
        );
    }
}
