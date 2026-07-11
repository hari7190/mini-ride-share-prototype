package foo.hari.rideshare.rider.entity;

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
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Same UUID as {@code users.id} in auth-service (JWT {@code sub}). */
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "rider_id", columnDefinition = "uuid")
    private UUID riderId;

    /**
     * Geographic point in WGS-84 (SRID 4326). Nullable until the driver reports a position.
     */
    @Column(name = "pickup_location", columnDefinition = "geometry(Point,4326)")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Point pickupLocation;

    @Column(name = "destination", columnDefinition = "geometry(Point,4326)")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Point destination;

    @Enumerated(EnumType.STRING)
    private RideStatus status;
    
}
