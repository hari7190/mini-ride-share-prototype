package com.zamorincorp.rideshare.rider.controller;

import com.zamorincorp.rideshare.rider.dto.RideRequestDTO;
import com.zamorincorp.rideshare.rider.entity.Trip;
import com.zamorincorp.rideshare.rider.entity.TripStatus;
import com.zamorincorp.rideshare.rider.service.RiderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderControllerTest {

    @Mock
    private RiderService riderService;

    @InjectMocks
    private RiderController riderController;

//    @Test
//    void requestRide_returnsCreatedWithTripId() {
//        RideRequestDTO dto = new RideRequestDTO();
//        dto.setPickupLocation("-79.38,43.65");
//        dto.setDestination("-79.40,43.70");
//
//        Trip saved = new Trip();
//        saved.setId(7L);
//        saved.setStatus(TripStatus.PENDING);
//
//        Jwt jwt = Jwt.withTokenValue("token")
//                .header("alg", "none")
//                .subject("rider-abc")
//                .issuedAt(Instant.now())
//                .expiresAt(Instant.now().plusSeconds(3600))
//                .build();
//
//        when(riderService.createRideRequest(dto, "rider-abc")).thenReturn(saved);
//
//        ResponseEntity<?> response = riderController.requestRide(dto, jwt);
//
//        assertEquals(HttpStatus.CREATED, response.getStatusCode());
//        assertEquals(7L, response.getBody());
//    }

//    @Test
//    void requestRide_whenServiceFails_returnsInternalServerError() {
//        RideRequestDTO dto = new RideRequestDTO();
//        Jwt jwt = Jwt.withTokenValue("token")
//                .header("alg", "none")
//                .subject("rider-abc")
//                .issuedAt(Instant.now())
//                .expiresAt(Instant.now().plusSeconds(3600))
//                .build();
//
//        when(riderService.createRideRequest(dto, "rider-abc"))
//                .thenThrow(new IllegalArgumentException("invalid location"));
//
//        ResponseEntity<?> response = riderController.requestRide(dto, jwt);
//
//        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
//        @SuppressWarnings("unchecked")
//        Map<String, String> body = (Map<String, String>) response.getBody();
//        assertEquals("Could not process ride request", body.get("error"));
//        assertEquals("invalid location", body.get("message"));
//    }
}
