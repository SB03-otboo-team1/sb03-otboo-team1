package com.onepiece.otboo.domain.auth.dto.response;

import com.onepiece.otboo.domain.user.dto.response.UserDto;

public record JwtDto(String accessToken, UserDto userDto) {

}
