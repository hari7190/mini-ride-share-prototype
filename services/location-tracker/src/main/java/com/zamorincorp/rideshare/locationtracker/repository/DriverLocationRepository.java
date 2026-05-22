package com.zamorincorp.rideshare.locationtracker.repository;

import com.zamorincorp.rideshare.locationtracker.entity.DriverLocation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, UUID> {}
