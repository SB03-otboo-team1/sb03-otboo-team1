package com.onepiece.otboo.domain.profile.fixture;

import com.onepiece.otboo.domain.profile.dto.response.ProfileDto;
import com.onepiece.otboo.domain.profile.enums.Gender;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ProfileDtoFixture {

    public static ProfileDto createProfile(UUID userId) {
        return ProfileDto.builder()
            .userId(userId)
            .name("test")
            .gender(Gender.MALE)
            .birthDate(LocalDate.of(1999, 7, 2))
            .location(new WeatherAPILocation(37.5665, 126.9780, 60, 127, List.of("서울", "중구")))
            .temperatureSensitivity(5)
            .profileImageUrl("http://example.com/profile.png")
            .build();
    }
}
