package com.zamorincorp.rideshare.locationtracker.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.zamorincorp.rideshare.locationtracker.service.LocationTrackerService;
import com.zamorincorp.rideshare.locationtracker.dto.DriverLocationDTO;

@RestController
@RequestMapping("/api/locationtracker")
public class DriverLocationController {

    @Autowired
    private LocationTrackerService locationTrackerService;

    @PatchMapping("/update")
    public ResponseEntity<String> updateDriverLocation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody DriverLocationDTO request) {
        locationTrackerService.updateDriverLocation(jwt.getSubject(), request);
        return ResponseEntity.ok("Location updated successfully");
    }
}
