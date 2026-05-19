package com.zamorincorp.rideshare.driver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Map;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.zamorincorp.rideshare.driver.service.DriverService;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    @Autowired
    private DriverService driverService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "subject", jwt.getSubject()));
    }

    // Get available ride requests for the driver
    @GetMapping("/get-ride-request")
    public ResponseEntity<List<Ride>> fetchRides(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(driverService.fetchRides(jwt.getSubject()));
    }

    // Accept a ride request
    @PostMapping("/accept-ride")
    public ResponseEntity<String> acceptRide(@AuthenticationPrincipal Jwt jwt, @RequestBody RideRequest rideRequest) {
        return ResponseEntity.ok(driverService.acceptRide(jwt.getSubject(), rideRequest));
    }

    // Decline a ride request
    @PostMapping("/decline-ride")
    public ResponseEntity<String> declineRide(@AuthenticationPrincipal Jwt jwt, @RequestBody RideRequest rideRequest) {
        return ResponseEntity.ok(driverService.declineRide(jwt.getSubject(), rideRequest));
    }
    
}
