package com.onepiece.otboo.domain.user.controller.api;

import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.request.UserGetRequest;
import com.onepiece.otboo.domain.user.dto.request.UserLockUpdateRequest;
import com.onepiece.otboo.domain.user.dto.request.UserRoleUpdateRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(
        summary = "계정 목록 조회"
        , description = "계정 목록 조회 API"
        , security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "계정 목록 조회 성공",
            content = @Content(
                mediaType = "*/*",
                array = @ArraySchema(schema = @Schema(implementation = CursorPageResponseDto.class))
            )
        ),
        @ApiResponse(
            responseCode = "400", description = "계정 목록 조회 실패",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<CursorPageResponseDto<UserDto>> getUsers(@Valid @ModelAttribute UserGetRequest request);

    @Operation(
        summary = "권한 수정",
        description = "권한 수정 API",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "권한 변경 성공"
        ),
        @ApiResponse(
            responseCode = "404", description = "권한 변경 실패(사용자 없음)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<Void> changeRole(@PathVariable("id") String userId,
        @RequestBody UserRoleUpdateRequest request);

    @Operation(
        summary = "계정 잠금 상태 변경",
        description = "[어드민 기능] 계정 잠금 상태를 변경합니다.",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "계정 잠금 상태 변경 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", description = "계정 잠금 상태 변경 실패(사용자 없음)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ResponseEntity<UserDto> updateUserLock(@PathVariable("userId") String userId,
        @Valid @RequestBody UserLockUpdateRequest request);
}
