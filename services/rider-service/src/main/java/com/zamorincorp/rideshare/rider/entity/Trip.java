package com.zamorincorp.rideshare.rider.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String riderId;
    private String pickupLocation;
    private String destination;

    @Enumerated(EnumType.STRING)
    private TripStatus status; 
    
}
