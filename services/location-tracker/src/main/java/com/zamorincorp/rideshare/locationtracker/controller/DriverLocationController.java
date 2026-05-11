package com.zamorincorp.rideshare.locationtracker.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import com.zamorincorp.rideshare.locationtracker.service.LocationTrackerService;
import com.zamorincorp.rideshare.locationtracker.dto.DriverLocationDTO;

@RestController
@RequestMapping("/api/locationtracker")
public class DriverLocationController {

    @Autowired
    private LocationTrackerService locationTrackerService;

    @PostMapping("/update")
    public ResponseEntity<String> updateDriverLocation(@RequestBody DriverLocationDTO request) {
        locationTrackerService.updateDriverLocation(request);
        return ResponseEntity.ok("Location updated successfully");
    }
}
