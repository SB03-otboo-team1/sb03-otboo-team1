package com.onepiece.otboo.domain.weather.controller;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiece.otboo.domain.location.service.LocationService;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import com.onepiece.otboo.global.config.JpaConfig;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(value = WeatherController.class,
    excludeAutoConfiguration = {JpaConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("WeatherController 단위 테스트")
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocationService locationService;

    @Test
    void 위치_정보_조회_성공시_200을_반환한다() throws Exception {

        // given
        double longitude = 126.8054724084087;
        double latitude = 37.43751196107601;

        WeatherAPILocation weatherAPILocation = new WeatherAPILocation(
            37.4375,
            126.8054,
            57,
            124,
            List.of("경기도", "시흥시", "은행동")
        );

        given(locationService.getLocation(anyDouble(), anyDouble())).willReturn(weatherAPILocation);

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
}