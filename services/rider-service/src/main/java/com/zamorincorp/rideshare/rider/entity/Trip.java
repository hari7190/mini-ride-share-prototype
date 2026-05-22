package com.zamorincorp.rideshare.rider.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "rides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Same UUID as {@code users.id} in auth-service (JWT {@code sub}). */
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "rider_id", length = 36, columnDefinition = "CHAR(36)")
    private UUID riderId;

    /**
     * Geographic point in WGS-84 (SRID 4326). Nullable until the driver reports a position.
     */
    @Column(name = "pickup_location", columnDefinition = "POINT SRID 4326")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Point pickupLocation;

    @Column(name = "destination", columnDefinition = "POINT SRID 4326")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Point destination;

    @Enumerated(EnumType.STRING)
    private TripStatus status; 
    
}
