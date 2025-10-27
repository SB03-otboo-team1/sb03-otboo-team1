package com.onepiece.otboo.domain.auth.dto.data;

import com.onepiece.otboo.domain.auth.dto.response.JwtDto;

public record RefreshTokenData(
    JwtDto jwtDto,
    String newRefreshToken
) {

}
