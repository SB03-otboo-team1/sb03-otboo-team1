package com.onepiece.otboo.domain.location.dto.data;

import com.onepiece.otboo.domain.location.entity.Location;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Builder;

@Builder
public record LocationDto(
    Double latitude,
    Double longitude,
    Integer x,
    Integer y,
    List<String> locationNames
) {

    public static LocationDto toDto(Location location) {
        return LocationDto.builder()
            .latitude(location.getLatitude())
            .longitude(location.getLongitude())
            .x(location.getXCoordinate())
            .y(location.getYCoordinate())
            .locationNames(splitLocationNames(location.getLocationNames()))
            .build();
    }

    private static List<String> splitLocationNames(String locationNames) {
        if (locationNames == null || locationNames.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(locationNames.split(","))
            .map(String::trim)
            .filter(name -> !name.isBlank())
            .toList();
    }
}
