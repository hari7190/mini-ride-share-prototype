package com.zamorincorp.rideshare.rider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.zamorincorp.rideshare.rider.entity.Ride;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

}
