package com.zamorincorp.rideshare.rider.dto;

public record RideRequestedEvent(
        String eventId,
        String eventType,
        String eventVersion,
        String occurredAt,
        Long tripId,
        String riderId,
        String pickupLocation,
        String destination,
        String status
) {
}
