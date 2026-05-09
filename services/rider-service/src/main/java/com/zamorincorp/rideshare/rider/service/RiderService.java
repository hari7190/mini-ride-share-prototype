package com.zamorincorp.rideshare.rider.service;

import org.springframework.stereotype.Service;
import com.zamorincorp.rideshare.rider.repository.TripRepository;
import com.zamorincorp.rideshare.rider.entity.Trip;
import com.zamorincorp.rideshare.rider.dto.RideRequestDTO;
import com.zamorincorp.rideshare.rider.entity.TripStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

@Service
public class RiderService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    //create createRideRequest method
    public Trip createRideRequest(RideRequestDTO rideRequestDTO, String riderId) {
        // 1. Convert DTO to Model (Mapping)
        Trip trip = new Trip();
        trip.setRiderId(riderId);
        trip.setPickupLocation(rideRequestDTO.getPickupLocation());
        trip.setDestination(rideRequestDTO.getDestination());
        trip.setStatus(TripStatus.PENDING);

        // 2. Save the trip to the database
        Trip saved = tripRepository.save(trip);

        // 3. Send the trip to the Kafka topic
        kafkaTemplate.send("ride-requests", saved.toString());

        return saved;
    }
}
