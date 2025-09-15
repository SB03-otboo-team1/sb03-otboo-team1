package com.onepiece.otboo.domain.location.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.onepiece.otboo.domain.location.fixture.LocationFixture;
import org.junit.jupiter.api.Test;

public class LocationTest {

    @Test
    void location_객체_생성() {
        Location location = LocationFixture.createLocation();
        assertThat(location.getLatitude()).isNotNull();
        assertThat(location.getLongitude()).isNotNull();
        assertThat(location.getXCoordinate()).isNotNull();
        assertThat(location.getYCoordinate()).isNotNull();
        assertThat(location.getLocationNames()).isNotNull();
    }
}
