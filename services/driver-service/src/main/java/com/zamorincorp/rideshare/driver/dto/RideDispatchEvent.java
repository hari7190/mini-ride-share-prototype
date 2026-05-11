package com.zamorincorp.rideshare.driver.dto;

import java.time.LocalDateTime;
public record RideDispatchEvent(
    Long tripId,
    String riderId,
    String driverId,
    String pickupLocation,
    String destination,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
