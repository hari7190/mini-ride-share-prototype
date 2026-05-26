package com.zamorincorp.rideshare.driver.dto;

import java.math.BigInteger;
import com.zamorincorp.rideshare.driver.entity.RideStatus;

public record RideDTO(BigInteger rideId, RideStatus status) { }
