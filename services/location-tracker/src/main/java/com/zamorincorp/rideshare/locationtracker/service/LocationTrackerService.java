package com.zamorincorp.rideshare.locationtracker.service;

import com.zamorincorp.rideshare.locationtracker.entity.DriverLocation;
import com.zamorincorp.rideshare.locationtracker.repository.DriverLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.zamorincorp.rideshare.locationtracker.dto.DriverLocationDTO;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationTrackerService {

    private static final GeometryFactory GEOMETRY_FACTORY =
        new GeometryFactory(new PrecisionModel(), 4326);

    private final DriverLocationRepository driverLocationRepository;

    public void updateDriverLocation(String driverId, DriverLocationDTO request) {
        DriverLocation driverLocation = DriverLocation.builder()
            .driverId(driverId)
            .currentLocation(convertToPoint(request.getCurrentLocation()))
            .updatedAt(Instant.now())
            .build();
        driverLocationRepository.save(driverLocation);
    }

    private Point convertToPoint(String location) {
        String[] parts = location.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("currentLocation must be \"longitude,latitude\"");
        }
        double longitude = Double.parseDouble(parts[0].trim());
        double latitude = Double.parseDouble(parts[1].trim());
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

}
