package com.onepiece.otboo.domain.weather.controller.api;


import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "날씨 관리", description = "날씨 관련 API")
public interface WeatherApi {

    @Operation(
        summary = "날씨 위치 정보 조회",
        description = "날씨 위치 정보 조회 API",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "날씨 위치 정보 조회 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = WeatherAPILocation.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", description = "날씨 위치 정보 조회 실패",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<WeatherAPILocation> getLocation(
        @RequestParam Double longitude,
        @RequestParam Double latitude
    );
}
