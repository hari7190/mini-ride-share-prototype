package com.zamorincorp.rideshare.rider.controller;

import com.zamorincorp.rideshare.rider.dto.RideRequestDTO;
import com.zamorincorp.rideshare.rider.entity.Trip;
import com.zamorincorp.rideshare.rider.service.RiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rider")
public class RiderController {

    @Autowired
    private RiderService riderService;

    /**
     * POST /api/rider/request
     * The @AuthenticationPrincipal Jwt jwt ensures that only valid tokens get here.
     */
    @PostMapping("/request")
    public ResponseEntity<?> requestRide(
            @RequestBody RideRequestDTO rideRequestDTO,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            // 1. Extract the unique User ID (Subject) from the JWT
            // This 'sub' claim was set by your Auth Service during login
            String riderId = jwt.getSubject();

            // 2. Optional: Extract other claims if your Auth Service provides them
            // String email = jwt.getClaim("email");

            // 3. Delegate business logic to the service
            Trip savedTrip = riderService.createRideRequest(rideRequestDTO, riderId);

            // 4. Return the saved trip with a 201 Created status
            return new ResponseEntity<>(savedTrip.getId(), HttpStatus.CREATED);

        } catch (Exception e) {
            // Basic error handling for your prototype
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not process ride request", "message", e.getMessage()));
        }
    }

    /**
     * GET /api/rider/status/{id}
     * Check the status of a specific trip
     */
    // @GetMapping("/status/{id}")
    // public ResponseEntity<Trip> getTripStatus(@PathVariable Long id) {
    //     // You would implement this method in your RiderService
    //     return riderService.getTripById(id)
    //             .map(ResponseEntity::ok)
    //             .orElse(ResponseEntity.notFound().build());
    // }
}
