package com.zamorincorp.rideshare.locationtracker.repository;

import com.zamorincorp.rideshare.locationtracker.entity.RiderLocation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiderLocationRepository extends JpaRepository<RiderLocation, UUID> {}

