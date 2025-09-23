package com.onepiece.otboo.domain.user.service;

import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.request.UserGetRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.UUID;

public interface UserService {

    UserDto create(UserCreateRequest userCreateRequest);

    CursorPageResponseDto<UserDto> getUsers(UserGetRequest userGetRequest);

    void changeRole(UUID userId, Role role);

    UserDto lockUser(UUID userId);

    UserDto unlockUser(UUID userId);
}
