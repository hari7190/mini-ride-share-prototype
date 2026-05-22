package com.zamorincorp.rideshare.driver.controller;

import com.zamorincorp.rideshare.driver.dto.RideDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Map;
import com.zamorincorp.rideshare.driver.service.DriverService;


@RestController
@RequestMapping("/api/driver")
public class DriverController {

    DriverService driverService = new DriverService();

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "subject", jwt.getSubject()));
    }

    // Get available ride requests for the driver
    @GetMapping("/ride-request")
    public ResponseEntity<RideDTO> fetchRides(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(driverService.fetchRide(jwt.getSubject()));
    }

    // Accept a ride request
    @PostMapping("/accept-ride")
    public ResponseEntity<String> acceptRide(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(driverService.acceptRide(jwt.getSubject()));
    }

    // Decline a ride request
    @PostMapping("/decline-ride")
    public ResponseEntity<String> declineRide(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(driverService.declineRide(jwt.getSubject()));
    }
    
}
