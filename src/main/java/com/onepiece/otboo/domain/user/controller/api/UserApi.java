package com.onepiece.otboo.domain.user.controller.api;

import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.global.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "프로필 관리", description = "프로필 관련 API")
public interface UserApi {

    @Operation(
        summary = "사용자 등록(회원가입)"
        , description = "사용자 등록(회원가입) API"
        , security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "사용자 등록(회원가입) 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", description = "사용자 등록(회원가입) 실패",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateRequest userCreateRequest);
}
