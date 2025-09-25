package com.onepiece.otboo.domain.profile.dto.request;

import com.onepiece.otboo.domain.profile.enums.Gender;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record ProfileUpdateRequest(
    @NotBlank
    @Size(min = 2, max = 20, message = "이름은 2~20자여야 합니다.")
    String name,

    Gender gender,

    LocalDate birthDate,

    WeatherAPILocation location,

    @Min(value = 1, message = "온도 민감도는 최소 1입니다.")
    @Max(value = 5, message = "온도 민감도는 최대 5입니다.")
    Integer temperatureSensitivity
) {

}
