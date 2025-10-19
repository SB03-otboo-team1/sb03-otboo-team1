package com.onepiece.otboo.domain.location.fixture;

import com.onepiece.otboo.domain.location.entity.Location;
import java.util.Random;

public class LocationFixture {

    private static final Random random = new Random();

    public static Location createLocation() {
        return Location.builder()
            .latitude(randomLatitude())
            .longitude(randomLongitude())
            .xCoordinate(randomXCoordinate())
            .yCoordinate(randomYCoordinate())
            .locationNames(randomLocationNames())
            .build();
    }

    private static double randomLatitude() {
        return 33.0 + (random.nextDouble() * (38.0 - 33.0));
    }

    private static double randomLongitude() {
        return 124.0 + (random.nextDouble() * (132.0 - 124.0));
    }

    private static int randomXCoordinate() {
        return 50 + random.nextInt(100);
    }

    private static int randomYCoordinate() {
        return 100 + random.nextInt(100);
    }

    private static String randomLocationNames() {
        String[] names = {"서울특별시,중구", "부산광역시,해운대구", "대구광역시,수성구", "광주광역시,동구", "제주시"};
        return names[random.nextInt(names.length)];
    }
}
