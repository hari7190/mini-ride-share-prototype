package foo.hari.rideshare.locationtracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "driver_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverLocation {

    /** Same UUID as {@code users.id} / {@code drivers.driver_id}. */
    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "driver_id", length = 36, columnDefinition = "CHAR(36)")
    private UUID driverId;

    /**
     * Geographic point in WGS-84 (SRID 4326). Nullable until the driver reports a position.
     */
    @Column(name = "current_location", columnDefinition = "POINT SRID 4326")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Point currentLocation;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
