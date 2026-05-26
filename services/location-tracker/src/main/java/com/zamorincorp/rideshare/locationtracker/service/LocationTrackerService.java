package com.zamorincorp.rideshare.locationtracker.service;

import com.zamorincorp.rideshare.locationtracker.entity.DriverLocation;
import com.zamorincorp.rideshare.locationtracker.entity.RiderLocation;
import com.zamorincorp.rideshare.locationtracker.repository.DriverLocationRepository;
import com.zamorincorp.rideshare.locationtracker.repository.RiderLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.zamorincorp.rideshare.locationtracker.dto.DriverLocationDTO;
import com.zamorincorp.rideshare.locationtracker.dto.RiderLocationDTO;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationTrackerService {

    private static final GeometryFactory GEOMETRY_FACTORY =
        new GeometryFactory(new PrecisionModel(), 4326);

    private final DriverLocationRepository driverLocationRepository;
    private final RiderLocationRepository riderLocationRepository;

    public void updateDriverLocation(String driverId, DriverLocationDTO request) {
        DriverLocation driverLocation = DriverLocation.builder()
            .driverId(UUID.fromString(driverId))
            .currentLocation(convertToPoint(request.getCurrentLocation()))
            .updatedAt(Instant.now())
            .build();
        driverLocationRepository.save(driverLocation);
    }

    public void updateRiderLocation(String riderId, RiderLocationDTO request) {
        RiderLocation riderLocation = RiderLocation.builder()
            .riderId(UUID.fromString(riderId))
            .currentLocation(convertToPoint(request.getCurrentLocation()))
            .updatedAt(Instant.now())
            .build();
        riderLocationRepository.save(riderLocation);
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
