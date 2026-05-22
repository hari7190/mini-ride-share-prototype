package com.zamorincorp.rideshare.locationtracker.controller;

import com.zamorincorp.rideshare.locationtracker.dto.DriverLocationDTO;
import com.zamorincorp.rideshare.locationtracker.service.LocationTrackerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DriverLocationControllerTest {

    @Mock
    private LocationTrackerService locationTrackerService;

    @InjectMocks
    private DriverLocationController driverLocationController;

    @Test
    void updateDriverLocation_returnsOkAndDelegatesToService() {
        DriverLocationDTO dto = new DriverLocationDTO();
        dto.setCurrentLocation("-79.38,43.65");

        String driverId = "550e8400-e29b-41d4-a716-446655440000";
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(driverId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        ResponseEntity<String> response = driverLocationController.updateDriverLocation(jwt, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Location updated successfully", response.getBody());
        verify(locationTrackerService).updateDriverLocation(driverId, dto);
    }
}
