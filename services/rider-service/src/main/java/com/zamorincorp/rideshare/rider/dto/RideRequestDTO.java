package com.zamorincorp.rideshare.rider.dto;

import lombok.Data;

@Data
public class RideRequestDTO {
    private String pickupLocation;
    private String destination;
}
