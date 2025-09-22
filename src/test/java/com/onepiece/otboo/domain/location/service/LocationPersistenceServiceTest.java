package com.onepiece.otboo.domain.location.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.fixture.LocationFixture;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class LocationPersistenceServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    LocationPersistenceService persistenceService;

    @Test
    void Location_저장_테스트() {

        // given
        Location location = LocationFixture.createLocation();
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(location, "id", id);

        given(locationRepository.save(any(Location.class))).willReturn(location);

        // when
        Location saved = persistenceService.save(location);

        // then
        assertNotNull(saved);
        assertEquals(id, saved.getId());
    }

    @Test
    void 위도_경도로_Loation_조회_테스트() {

        // given
        Location location = LocationFixture.createLocation();
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(location, "id", id);

        given(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble()))
            .willReturn(Optional.of(location));

        // when
        Optional<Location> result = persistenceService.findByLatitudeAndLongitude(location.getLatitude(),
            location.getLongitude());

        // then
        assertThat(result).isPresent();
        assertEquals(location.getLocationNames(), result.get().getLocationNames());
    }
}