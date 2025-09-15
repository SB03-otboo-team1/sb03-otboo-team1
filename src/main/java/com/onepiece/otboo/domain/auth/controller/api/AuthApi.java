package com.onepiece.otboo.domain.auth.controller.api;

import com.onepiece.otboo.domain.auth.dto.request.SignInRequest;
import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.global.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "인증 관리", description = "인증 관련 API")
public interface AuthApi {

    @Operation(summary = "CSRF 토큰 조회", description = "CSRF 토큰을 조회합니다. 토큰은 쿠키(XSRF-TOKEN)에 저장됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "CSRF 토큰 조회 성공"),
        @ApiResponse(responseCode = "400", description = "CSRF 토큰 조회 실패")
    })
    ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken);

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 입력하여 로그인합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = JwtDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "인증 입력값 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "로그인 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "토큰 생성 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @RequestBody(
        required = true,
        content = @Content(
            mediaType = "multipart/form-data"
        )
    )
    void signIn(@ModelAttribute SignInRequest signInRequest);
}
