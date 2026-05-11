package com.zamorincorp.rideshare.rider.dto;

import java.time.LocalDateTime;

public record DriverAssignedEventDTO (
    Long tripId,    
    String riderId,
    String driverId,
    String pickupLocation,
    String destination,
    LocalDateTime createdAt,
    LocalDateTime updatedAt)
{}
