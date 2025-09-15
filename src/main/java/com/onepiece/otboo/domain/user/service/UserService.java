package com.onepiece.otboo.domain.user.service;

import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;

public interface UserService {

    UserDto create(UserCreateRequest userCreateRequest);
}
