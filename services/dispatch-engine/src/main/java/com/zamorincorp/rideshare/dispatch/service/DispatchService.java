package com.zamorincorp.rideshare.dispatch.service;

import com.zamorincorp.rideshare.dispatch.dto.RideRequestedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.zamorincorp.rideshare.dispatch.entity.RideDispatch;
import com.zamorincorp.rideshare.dispatch.entity.DispatchStatus;
import com.zamorincorp.rideshare.dispatch.repository.RideDispatchRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Slf4j
public class DispatchService {
    @Autowired
    private RideDispatchRepository rideDispatchRepository;

    public void handleRideRequested(RideRequestedEvent event) {
        log.info(
                "Dispatch received ride request: tripId={} riderId={} pickup={} destination={} type={} version={}",
                event.tripId(),
                event.riderId(),
                event.pickupLocation(),
                event.destination(),
                event.eventType(),
                event.eventVersion());
        
        // 1. Create a new ride dispatch record
        RideDispatch rideDispatch = new RideDispatch();
        rideDispatch.setTripId(event.tripId());
        rideDispatch.setRiderId(event.riderId());
        rideDispatch.setStatus(DispatchStatus.SEARCHING);
        rideDispatchRepository.save(rideDispatch);

        log.info("Ride dispatch created for tripId={} riderId={}", event.tripId(), event.riderId());
    
        // Mock matching logic

        String driverId = "driver123";

        // 2. Update the ride dispatch record with the driver id
        rideDispatch.setDriverId(driverId);
        rideDispatch.setStatus(DispatchStatus.MATCHED);
        rideDispatchRepository.save(rideDispatch);

        log.info("Ride dispatch updated for tripId={} riderId={} driverId={}", event.tripId(), event.riderId(), driverId);

        // 3. Publish a ride matched event
        RideMatchedEvent rideMatchedEvent = new RideMatchedEvent(
            event.tripId(),
            event.riderId(),
            driverId
        );
        kafkaTemplate.send(rideMatchedEvent);
    }
}
