package foo.hari.rideshare.dispatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import foo.hari.rideshare.dispatch.entity.RideDispatch;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface RideDispatchRepository extends JpaRepository<RideDispatch, Long> {
    Optional<RideDispatch> findByTripId(Long tripId);
}
