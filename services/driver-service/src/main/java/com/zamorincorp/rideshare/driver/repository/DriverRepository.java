package com.zamorincorp.rideshare.driver.repository;

import com.zamorincorp.rideshare.driver.entity.Driver;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {}
