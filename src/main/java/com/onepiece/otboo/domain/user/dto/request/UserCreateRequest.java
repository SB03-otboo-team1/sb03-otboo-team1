package com.onepiece.otboo.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
    @NotBlank
    @Size(min = 2, max = 20, message = "이름은 2~20자여야 합니다.")
    @Pattern(
        regexp = "^[가-힣a-zA-Z0-9]+$",
        message = "이름은 한글, 영문, 숫자만 입력 가능합니다."
    )
    String name,

    @NotBlank
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    String email,

    @NotBlank
    @Size(min = 6, max = 100, message = "비밀번호는 6~100자여야 합니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z0-9@$!%*#?&]+$",
        message = "비밀번호는 영어, 숫자, 특수문자를 포함해야 합니다."
    )
    String password
) {

}
