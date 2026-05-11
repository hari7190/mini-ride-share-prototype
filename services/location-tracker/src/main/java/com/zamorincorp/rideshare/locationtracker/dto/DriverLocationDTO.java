package com.zamorincorp.rideshare.locationtracker.dto;

import lombok.Data;

@Data
public class DriverLocationDTO {
    private String driverId;
    private String currentLocation;
}
