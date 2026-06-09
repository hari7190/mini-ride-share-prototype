package foo.hari.rideshare.rider.dto;

import java.time.LocalDateTime;

public record RideDeclinedEventDTO(
        Long tripId,
        String driverId,
        String status,
        LocalDateTime declinedAt,
        LocalDateTime occurredAt
) { }
