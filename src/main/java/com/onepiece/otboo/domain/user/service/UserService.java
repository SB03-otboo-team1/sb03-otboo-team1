package com.onepiece.otboo.domain.user.service;

import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.enums.Role;
import java.util.UUID;

public interface UserService {

    UserDto create(UserCreateRequest userCreateRequest);

    void changeRole(UUID userId, Role role);

    UserDto lockUser(UUID userId);

    UserDto unlockUser(UUID userId);
}
