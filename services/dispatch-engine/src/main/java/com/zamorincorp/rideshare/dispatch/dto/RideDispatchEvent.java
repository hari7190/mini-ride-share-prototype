package com.zamorincorp.rideshare.dispatch.dto;

import java.time.LocalDateTime;

import com.zamorincorp.rideshare.dispatch.entity.DispatchStatus;

public record RideDispatchEvent (
    Long tripId,    
    String riderId,
    String driverId,
    DispatchStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt)
{}
