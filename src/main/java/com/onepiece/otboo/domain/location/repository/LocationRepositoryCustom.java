package com.onepiece.otboo.domain.location.repository;

import com.onepiece.otboo.domain.location.entity.Location;
import java.util.Optional;

public interface LocationRepositoryCustom {
    Optional<Location> findNearest(double latitude, double longitude);
}
