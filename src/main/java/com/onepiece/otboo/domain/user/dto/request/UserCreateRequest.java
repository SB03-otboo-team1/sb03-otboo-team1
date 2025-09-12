package com.onepiece.otboo.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
    @NotBlank
    @Size(max = 100, message = "이름은 100자까지만 입력 가능합니다.")
    String name,

    @NotBlank
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    String email,

    @NotBlank
    @Pattern(
        regexp = "(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{6,100}$",
        message = "비밀번호는 영어, 숫자, 특수문자를 포함하여 6자 이상 입력해야 합니다."
    )
    String password
) {

}
