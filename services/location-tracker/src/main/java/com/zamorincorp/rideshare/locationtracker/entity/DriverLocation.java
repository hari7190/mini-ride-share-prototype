package com.zamorincorp.rideshare.locationtracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.Instant;

@Entity
@Table(name = "driver_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverLocation {

    @Id
    @Column(name = "driver_id", nullable = false, length = 64)
    private String driverId;

    /**
     * Geographic point in WGS-84 (SRID 4326). Nullable until the driver reports a position.
     */
    @Column(name = "current_location", columnDefinition = "POINT SRID 4326")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Point currentLocation;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
