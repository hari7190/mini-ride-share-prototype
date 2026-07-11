package foo.hari.rideshare.dispatch.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Read-only native queries against {@code driver_locations} (written by location-tracker).
 */
@Repository
public class DriverLocationQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public DriverLocationQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Picks the driver closest to the pickup point using PostGIS geography distance (meters).
     * Only considers rows with a non-null {@code current_location}.
     *
     * @param longitude degrees (WGS-84)
     * @param latitude  degrees (WGS-84)
     */
    public Optional<String> findNearestDriverId(double longitude, double latitude) {
        String sql = """
            SELECT drivers.driver_id::text
            FROM driver_locations
            INNER JOIN drivers ON driver_locations.driver_id = drivers.driver_id
            WHERE current_location IS NOT NULL AND drivers.status = 'ONLINE'
            ORDER BY ST_Distance(
                current_location::geography,
                ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
            )
            LIMIT 1
            """;
        List<String> ids = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> rs.getString("driver_id"),
            longitude,
            latitude);
        return ids.stream().findFirst();
    }
}
