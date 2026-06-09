package foo.hari.rideshare.driver.dto;

import java.math.BigInteger;
import foo.hari.rideshare.driver.entity.RideStatus;

public record RideDTO(BigInteger rideId, RideStatus status) { }
