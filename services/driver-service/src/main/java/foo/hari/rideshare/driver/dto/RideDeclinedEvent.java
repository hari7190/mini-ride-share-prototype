package foo.hari.rideshare.driver.dto;

import foo.hari.rideshare.driver.entity.RideStatus;
import java.math.BigInteger;
import java.time.LocalDateTime;

public record RideDeclinedEvent(
        BigInteger tripId,
        String driverId,
        RideStatus status,
        LocalDateTime declinedAt,
        LocalDateTime occurredAt
) { }
