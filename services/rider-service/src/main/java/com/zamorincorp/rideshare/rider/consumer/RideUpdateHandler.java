package com.zamorincorp.rideshare.rider.consumer;

//this class will listen for driverassigned kafka topics and update the trip status table in the database

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamorincorp.rideshare.rider.dto.DriverAssignedEventDTO;
import com.zamorincorp.rideshare.rider.service.RiderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.zamorincorp.rideshare.rider.repository.TripRepository;
import com.zamorincorp.rideshare.rider.entity.Trip;
import com.zamorincorp.rideshare.rider.entity.TripStatus;


@Component
@Slf4j
@RequiredArgsConstructor
public class RideUpdateHandler {
    private final ObjectMapper objectMapper;
    
    @Autowired
    RiderService riderService;
    
    @Autowired
    TripRepository tripRepository;

    @KafkaListener(topics = "${app.kafka.topics.driver-assigned}")
    public void onDriverAssigned(String payload) {
        try {
            DriverAssignedEventDTO event = objectMapper.readValue(payload, DriverAssignedEventDTO.class);
            Trip trip = tripRepository.findById(event.tripId()).orElseThrow(() -> new RuntimeException("Trip not found"));
            trip.setStatus(TripStatus.ACCEPTED);
            tripRepository.save(trip);
        } catch (Exception e) {
            log.error("Failed to process driver assigned message payload={}", payload, e);
        }
    }
}
