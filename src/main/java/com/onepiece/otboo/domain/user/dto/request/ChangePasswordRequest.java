package com.onepiece.otboo.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

    @NotBlank
    @Size(min = 6, max = 100, message = "비밀번호는 6~100자여야 합니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z0-9@$!%*#?&]+$",
        message = "비밀번호는 영어, 숫자, 특수문자를 포함해야 합니다."
    )
    String password
) {

}
