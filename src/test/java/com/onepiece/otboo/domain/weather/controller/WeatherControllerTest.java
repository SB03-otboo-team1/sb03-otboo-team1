package com.onepiece.otboo.domain.weather.controller;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiece.otboo.domain.location.service.LocationService;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import com.onepiece.otboo.domain.weather.dto.response.HumidityDto;
import com.onepiece.otboo.domain.weather.dto.response.PrecipitationDto;
import com.onepiece.otboo.domain.weather.dto.response.TemperatureDto;
import com.onepiece.otboo.domain.weather.dto.response.WeatherDto;
import com.onepiece.otboo.domain.weather.dto.response.WindSpeedDto;
import com.onepiece.otboo.domain.weather.enums.PrecipitationType;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import com.onepiece.otboo.domain.weather.enums.WindSpeedWord;
import com.onepiece.otboo.domain.weather.service.WeatherService;
import com.onepiece.otboo.global.config.JpaConfig;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ActiveProfiles("test")
@WebMvcTest(value = WeatherController.class,
    excludeAutoConfiguration = {JpaConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("WeatherController 단위 테스트")
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private WeatherService weatherService;

    private double longitude = 126.8054724084087;
    private double latitude = 37.43751196107601;
    private WeatherAPILocation location;

    @BeforeEach
    void setUp() {
        longitude = 126.8054724084087;
        latitude = 37.43751196107601;

        location = new WeatherAPILocation(
            37.4375,
            126.8054,
            57,
            124,
            List.of("경기도", "시흥시", "은행동")
        );
    }

    @Test
    void 위치_정보_조회_성공시_200을_반환한다() throws Exception {

        // given
        given(locationService.getLocation(anyDouble(), anyDouble())).willReturn(location);

        // when
        ResultActions result = mockMvc.perform(get("/api/weathers/location")
            .param("longitude", String.valueOf(longitude))
            .param("latitude", String.valueOf(latitude))
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.x").value(String.valueOf(57)))
            .andExpect(jsonPath("$.y").value(String.valueOf(124)))
            .andExpect(jsonPath("$.locationNames[0]").value("경기도"));
        verify(locationService).getLocation(longitude, latitude);
    }

    @Test
    void 날씨_정보_조회_성공시_200을_반환한다() throws Exception {

        // given
        UUID weatherId = UUID.randomUUID();

        WeatherDto weatherDto = WeatherDto.builder()
            .id(weatherId)
            .forecastedAt(ZonedDateTime.parse("2025-09-20T05:00:00+09:00"))
            .forecastAt(ZonedDateTime.parse("2025-09-21T14:00:00+09:00"))
            .location(location)
            .skyStatus(SkyStatus.CLOUDY)
            .precipitation(new PrecipitationDto(PrecipitationType.NONE, 0.0, 30.0))
            .humidity(new HumidityDto(55.0, -5.0))
            .temperature(new TemperatureDto(22.0, -2.0, 18.0, 24.0))
            .windSpeed(new WindSpeedDto(1.0, WindSpeedWord.WEAK))
            .build();

        given(weatherService.getWeather(anyDouble(), anyDouble()))
            .willReturn(List.of(weatherDto));

        // when
        ResultActions result = mockMvc.perform(get("/api/weathers")
            .param("longitude", String.valueOf(longitude))
            .param("latitude", String.valueOf(latitude))
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(weatherId.toString()))
            .andExpect(jsonPath("$[0].skyStatus").value("CLOUDY"))
            .andExpect(jsonPath("$[0].precipitation.type").value("NONE"))
            .andExpect(jsonPath("$[0].humidity.current").value("55.0"))
            .andExpect(jsonPath("$[0].temperature.current").value("22.0"))
            .andExpect(jsonPath("$[0].windSpeed.asWord").value("WEAK"));
        verify(weatherService).getWeather(longitude, latitude);
    }
}