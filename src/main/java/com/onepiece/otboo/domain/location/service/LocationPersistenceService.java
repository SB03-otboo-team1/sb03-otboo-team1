package com.onepiece.otboo.domain.location.service;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationPersistenceService {

    private final LocationRepository locationRepository;

    @Transactional
    public Location save(Location location) {
        return locationRepository.save(location);
    }

    @Transactional(readOnly = true)
    public Optional<Location> findByLatitudeAndLongitude(double latitude, double longitude) {
        return locationRepository.findByLatitudeAndLongitude(latitude, longitude);
    }
}
