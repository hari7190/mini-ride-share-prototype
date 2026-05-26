package com.zamorincorp.rideshare.rider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamorincorp.rideshare.rider.dto.RideRequestDTO;
import com.zamorincorp.rideshare.rider.dto.RideRequestedEvent;
import com.zamorincorp.rideshare.rider.entity.Ride;
import com.zamorincorp.rideshare.rider.entity.RideStatus;
import com.zamorincorp.rideshare.rider.repository.RideRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class RiderServiceTest {

    private static final UUID RIDER_ID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    @Mock
    private RideRepository rideRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RiderService riderService;

    @Captor
    private ArgumentCaptor<Ride> tripCaptor;

    @Captor
    private ArgumentCaptor<Message<String>> messageCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(riderService, "rideRequestsTopic", "ride-requests.v1");
        ReflectionTestUtils.setField(riderService, "rideRequestedEventType",
                "com.zamorincorp.rideshare.rider.dto.RideRequestedEventDTO");
        ReflectionTestUtils.setField(riderService, "rideRequestedEventVersion", "1");
    }

    @Test
    void createRideRequest_savesTripAndPublishesEvent() throws Exception {
        RideRequestDTO dto = sampleRideRequest();
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride trip = invocation.getArgument(0);
            trip.setId(42L);
            return trip;
        });
        when(objectMapper.writeValueAsString(any(RideRequestedEvent.class))).thenReturn("{\"ok\":true}");

        Ride result = riderService.createRideRequest(dto, RIDER_ID.toString());

        verify(rideRepository).save(tripCaptor.capture());
        Ride savedTrip = tripCaptor.getValue();
        assertEquals(RIDER_ID, savedTrip.getRiderId());
        assertEquals(RideStatus.PENDING, savedTrip.getStatus());
        assertEquals(-79.38, savedTrip.getPickupLocation().getX(), 0.001);
        assertEquals(43.65, savedTrip.getPickupLocation().getY(), 0.001);
        assertEquals(-79.40, savedTrip.getDestination().getX(), 0.001);
        assertEquals(43.70, savedTrip.getDestination().getY(), 0.001);

        assertEquals(42L, result.getId());

        verify(kafkaTemplate).send(messageCaptor.capture());
        Message<String> message = messageCaptor.getValue();
        assertEquals("{\"ok\":true}", message.getPayload());
        assertEquals("ride-requests.v1", message.getHeaders().get(KafkaHeaders.TOPIC));
        assertEquals("42", message.getHeaders().get(KafkaHeaders.KEY));
        assertEquals("com.zamorincorp.rideshare.rider.dto.RideRequestedEventDTO",
                message.getHeaders().get("eventType"));
        assertEquals("1", message.getHeaders().get("eventVersion"));
    }

    @Test
    void createRideRequest_whenPickupFormatInvalid_throwsIllegalArgumentException() {
        RideRequestDTO dto = new RideRequestDTO(
                null, "POINT (-79.38 43.65)", "-79.40,43.70");

        assertThrows(IllegalArgumentException.class,
                () -> riderService.createRideRequest(dto, RIDER_ID.toString()));

        verify(rideRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(Message.class));
    }

    @Test
    void createRideRequest_whenSerializationFails_throwsIllegalStateException() throws Exception {
        RideRequestDTO dto = sampleRideRequest();
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride trip = invocation.getArgument(0);
            trip.setId(42L);
            return trip;
        });
        doThrow(new JsonProcessingException("boom") { })
                .when(objectMapper).writeValueAsString(any(RideRequestedEvent.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> riderService.createRideRequest(dto, RIDER_ID.toString()));
        assertTrue(ex.getMessage().contains("Could not serialize ride request event"));
        verify(kafkaTemplate, never()).send(any(Message.class));
    }

    private RideRequestDTO sampleRideRequest() {
        return new RideRequestDTO(null, "-79.38,43.65", "-79.40,43.70");
    }
}
