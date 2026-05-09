package com.zamorincorp.rideshare.dispatch.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ride_dispatch")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideDispatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long tripId; // Link to the Rider Service's Trip

    private String riderId;
    
    private String driverId;

    @Enumerated(EnumType.STRING)
    private DispatchStatus status; // SEARCHING, MATCHED, EXPIRED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }
    
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
