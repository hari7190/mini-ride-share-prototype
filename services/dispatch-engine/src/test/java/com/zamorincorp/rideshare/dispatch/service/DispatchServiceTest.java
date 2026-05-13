package com.zamorincorp.rideshare.dispatch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamorincorp.rideshare.dispatch.dto.RideDispatchEvent;
import com.zamorincorp.rideshare.dispatch.dto.RideRequestedEvent;
import com.zamorincorp.rideshare.dispatch.entity.DispatchStatus;
import com.zamorincorp.rideshare.dispatch.entity.RideDispatch;
import com.zamorincorp.rideshare.dispatch.repository.DriverLocationQueryRepository;
import com.zamorincorp.rideshare.dispatch.repository.RideDispatchRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

    @Mock
    private RideDispatchRepository rideDispatchRepository;

    @Mock
    private DriverLocationQueryRepository driverLocationQueryRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DispatchService dispatchService;

    @Captor
    private ArgumentCaptor<RideDispatch> rideDispatchCaptor;

    @Captor
    private ArgumentCaptor<Message<String>> messageCaptor;

    private List<DispatchStatus> savedStatuses;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dispatchService, "rideRequestsTopic", "driver-assigned.v1");
        ReflectionTestUtils.setField(dispatchService, "rideDispatchEventType", "com.zamorincorp.rideshare.dispatch.dto.RideDispatchEvent");
        ReflectionTestUtils.setField(dispatchService, "rideDispatchEventVersion", "1.0.0");

        savedStatuses = new ArrayList<>();
        when(rideDispatchRepository.save(any(RideDispatch.class))).thenAnswer(invocation -> {
            RideDispatch rideDispatch = invocation.getArgument(0);
            savedStatuses.add(rideDispatch.getStatus());
            if (rideDispatch.getId() == null) {
                rideDispatch.setId(1L);
            }
            return rideDispatch;
        });
    }

    @Test
    void handleRideRequested_whenDriverFound_marksMatchedAndPublishesEvent() throws Exception {
        RideRequestedEvent event = sampleRideRequestedEvent("79.1,43.6");
        when(driverLocationQueryRepository.findNearestDriverId(79.1, 43.6)).thenReturn(Optional.of("driver123"));
        when(objectMapper.writeValueAsString(any(RideDispatchEvent.class))).thenReturn("{\"ok\":true}");

        dispatchService.handleRideRequested(event);

        verify(rideDispatchRepository, org.mockito.Mockito.times(2)).save(rideDispatchCaptor.capture());
        assertEquals(List.of(DispatchStatus.SEARCHING, DispatchStatus.MATCHED), savedStatuses);
        List<RideDispatch> savedDispatches = rideDispatchCaptor.getAllValues();
        assertEquals("driver123", savedDispatches.get(1).getDriverId());

        verify(kafkaTemplate).send(messageCaptor.capture());
        Message<String> message = messageCaptor.getValue();
        assertEquals("{\"ok\":true}", message.getPayload());
        assertEquals("driver-assigned.v1", message.getHeaders().get(KafkaHeaders.TOPIC));
        assertEquals("1", message.getHeaders().get(KafkaHeaders.KEY));
        assertEquals("com.zamorincorp.rideshare.dispatch.dto.RideDispatchEvent", message.getHeaders().get("eventType"));
        assertEquals("1.0.0", message.getHeaders().get("eventVersion"));
    }

    @Test
    void handleRideRequested_whenNoDriverFound_marksFailedAndDoesNotPublish() {
        RideRequestedEvent event = sampleRideRequestedEvent("79.1,43.6");
        when(driverLocationQueryRepository.findNearestDriverId(79.1, 43.6)).thenReturn(Optional.empty());

        dispatchService.handleRideRequested(event);

        verify(rideDispatchRepository, org.mockito.Mockito.times(2)).save(rideDispatchCaptor.capture());
        assertEquals(List.of(DispatchStatus.SEARCHING, DispatchStatus.FAILED), savedStatuses);
        verify(kafkaTemplate, never()).send(any(Message.class));
    }

    @Test
    void handleRideRequested_whenPickupFormatInvalid_skipsLookupAndMarksFailed() {
        RideRequestedEvent event = sampleRideRequestedEvent("POINT (-79.642 43.6)");

        dispatchService.handleRideRequested(event);

        verify(driverLocationQueryRepository, never()).findNearestDriverId(any(Double.class), any(Double.class));
        verify(rideDispatchRepository, org.mockito.Mockito.times(2)).save(rideDispatchCaptor.capture());
        assertEquals(List.of(DispatchStatus.SEARCHING, DispatchStatus.FAILED), savedStatuses);
        verify(kafkaTemplate, never()).send(any(Message.class));
    }

    @Test
    void handleRideRequested_whenSerializationFails_throwsIllegalStateException() throws Exception {
        RideRequestedEvent event = sampleRideRequestedEvent("79.1,43.6");
        when(driverLocationQueryRepository.findNearestDriverId(79.1, 43.6)).thenReturn(Optional.of("driver123"));
        doThrow(new JsonProcessingException("boom") { }).when(objectMapper).writeValueAsString(any(RideDispatchEvent.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> dispatchService.handleRideRequested(event));
        assertTrue(ex.getMessage().contains("Could not serialize ride dispatch event"));
        verify(kafkaTemplate, never()).send(any(Message.class));
    }

    private RideRequestedEvent sampleRideRequestedEvent(String pickupLocation) {
        return new RideRequestedEvent(
            "evt-1",
            "ride-requested",
            "1.0.0",
            "2026-05-13T00:00:00Z",
            42L,
            "user111",
            pickupLocation,
            "79.2,43.7",
            "PENDING"
        );
    }
}
