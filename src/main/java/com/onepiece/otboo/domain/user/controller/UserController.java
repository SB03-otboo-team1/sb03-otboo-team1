package com.onepiece.otboo.domain.user.controller;

import com.onepiece.otboo.domain.user.api.UserApi;
import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateRequest userCreateRequest) {

        log.info("[UserController] 회원가입 요청 - email: {}, name: {}",
            userCreateRequest.email(), userCreateRequest.name());

        UserDto result = userService.create(userCreateRequest);

        log.info("[UserController] 회원가입 성공 - id: {}, email: {}",
            result.id(), result.email());

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
