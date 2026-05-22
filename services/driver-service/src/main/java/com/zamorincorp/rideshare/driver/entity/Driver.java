package com.zamorincorp.rideshare.driver.entity;

import jakarta.persistence.*;
import java.math.BigInteger;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {
    /** Same UUID as {@code users.id} in auth-service. */
    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "driver_id", length = 36, columnDefinition = "CHAR(36)")
    private UUID driverId;

    private String name;

    @Enumerated(EnumType.STRING)
    private DriverStatus status; // ONLINE, BUSY, OFFLINE

    private String currentVehicle;

    private BigInteger currentTripId;
}
