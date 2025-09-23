package com.onepiece.otboo.domain.profile.dto.response;

import com.onepiece.otboo.domain.profile.enums.Gender;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProfileDto(
    UUID userId,
    String name,
    Gender gender,
    LocalDate birthDate,
    WeatherAPILocation location,
    Integer temperatureSensitivity,
    String profileImageUrl
) {

}
