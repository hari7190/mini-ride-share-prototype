package com.zamorincorp.rideshare.locationtracker.service;

import com.zamorincorp.rideshare.locationtracker.dto.DriverLocationDTO;
import com.zamorincorp.rideshare.locationtracker.entity.DriverLocation;
import com.zamorincorp.rideshare.locationtracker.repository.DriverLocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationTrackerServiceTest {

    @Mock
    private DriverLocationRepository driverLocationRepository;

    @InjectMocks
    private LocationTrackerService locationTrackerService;

    @Captor
    private ArgumentCaptor<DriverLocation> driverLocationCaptor;

    @Test
    void updateDriverLocation_savesDriverLocationWithCoordinates() {
        DriverLocationDTO dto = new DriverLocationDTO();
        dto.setCurrentLocation("-79.38,43.65");
        when(driverLocationRepository.save(any(DriverLocation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        locationTrackerService.updateDriverLocation("driver-9", dto);

        verify(driverLocationRepository).save(driverLocationCaptor.capture());
        DriverLocation saved = driverLocationCaptor.getValue();
        assertEquals("driver-9", saved.getDriverId());
        assertEquals(-79.38, saved.getCurrentLocation().getX(), 0.001);
        assertEquals(43.65, saved.getCurrentLocation().getY(), 0.001);
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void updateDriverLocation_whenLocationFormatInvalid_throwsIllegalArgumentException() {
        DriverLocationDTO dto = new DriverLocationDTO();
        dto.setCurrentLocation("POINT (-79.38 43.65)");

        assertThrows(IllegalArgumentException.class,
                () -> locationTrackerService.updateDriverLocation("driver-9", dto));

        verify(driverLocationRepository, never()).save(any());
    }
}
