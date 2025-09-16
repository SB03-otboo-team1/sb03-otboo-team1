package com.onepiece.otboo.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignInRequest(
    @Schema(description = "이메일")
    @NotBlank
    @Email
    String username,

    @Schema(description = "비밀번호")
    @NotBlank
    String password
) {

}
