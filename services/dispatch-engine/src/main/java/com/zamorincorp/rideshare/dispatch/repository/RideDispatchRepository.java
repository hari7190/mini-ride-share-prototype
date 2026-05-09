package com.zamorincorp.rideshare.dispatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.zamorincorp.rideshare.dispatch.entity.RideDispatch;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface RideDispatchRepository extends JpaRepository<RideDispatch, Long> {
    Optional<RideDispatch> findByTripId(Long tripId);
}
