package com.zamorincorp.rideshare.driver.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {
    @Id
    private String driverId; // e.g., "driver123"
    private String name;
    
    @Enumerated(EnumType.STRING)
    private DriverStatus status; // ONLINE, BUSY, OFFLINE
    
    private String currentVehicle;
}
