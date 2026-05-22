package com.zamorincorp.rideshare.driver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {
    @Id
    private BigInteger driverId; // e.g., "driver123"
    private String name;
    
    @Enumerated(EnumType.STRING)
    private DriverStatus status; // ONLINE, BUSY, OFFLINE
    
    private String currentVehicle;

    private BigInteger currentTripId;
}
