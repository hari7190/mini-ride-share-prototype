package com.zamorincorp.rideshare.rider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.zamorincorp.rideshare.rider.entity.Trip;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

}
