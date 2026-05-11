package com.zamorincorp.rideshare.driver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.zamorincorp.rideshare.driver.entity.Driver;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {
    Optional<Driver> findByDriverId(String driverId);
}
