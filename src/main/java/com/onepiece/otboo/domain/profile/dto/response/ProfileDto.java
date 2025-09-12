package com.onepiece.otboo.domain.profile.dto.response;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.profile.enums.Gender;
import java.time.LocalDate;
import java.util.UUID;

public record ProfileDto(
    UUID userId,
    String name,
    Gender gender,
    LocalDate birthDate,
    Location location,
    Integer temperatureSensitivity,
    String profileImageUrl
) {

}
