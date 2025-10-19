package com.onepiece.otboo.domain.location.repository;

import com.onepiece.otboo.domain.location.entity.Location;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, UUID>,
    LocationRepositoryCustom {

    Optional<Location> findByLatitudeAndLongitude(double latitude, double longitude);

    Optional<Location> findByLocationNames(String name);
}
