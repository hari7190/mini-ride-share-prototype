package com.zamorincorp.rideshare.rider.service;

import org.springframework.stereotype.Service;
import com.zamorincorp.rideshare.rider.repository.TripRepository;
import com.zamorincorp.rideshare.rider.entity.Trip;
import com.zamorincorp.rideshare.rider.dto.RideRequestDTO;
import com.zamorincorp.rideshare.rider.entity.TripStatus;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class RiderService {

    @Autowired
    private TripRepository tripRepository;

    //create createRideRequest method
    public Trip createRideRequest(RideRequestDTO rideRequestDTO, String riderId) {
        // 1. Convert DTO to Model (Mapping)
        Trip trip = new Trip();
        trip.setRiderId(riderId);
        trip.setPickupLocation(rideRequestDTO.getPickupLocation());
        trip.setDestination(rideRequestDTO.getDestination());
        trip.setStatus(TripStatus.PENDING);

        // 2. Save the trip to the database
        return tripRepository.save(trip);
    }
}
