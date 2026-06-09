package foo.hari.rideshare.rider.dto;

import java.math.BigInteger;

public record RideRequestDTO(BigInteger rideId, String pickupLocation, String destination) { }
