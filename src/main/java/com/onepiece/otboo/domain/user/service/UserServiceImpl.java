package com.onepiece.otboo.domain.user.service;

import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public UserDto create(UserCreateRequest userCreateRequest) {
        return null;
    }
}
