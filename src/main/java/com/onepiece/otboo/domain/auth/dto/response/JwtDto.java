package com.onepiece.otboo.domain.auth.dto.response;

import com.onepiece.otboo.domain.user.dto.response.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtDto {

    private String accessToken;
    private UserDto userDto;
}