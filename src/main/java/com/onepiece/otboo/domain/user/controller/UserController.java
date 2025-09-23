package com.onepiece.otboo.domain.user.controller;

import com.onepiece.otboo.domain.user.controller.api.UserApi;
import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.request.UserGetRequest;
import com.onepiece.otboo.domain.user.dto.request.UserRoleUpdateRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.service.UserService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import jakarta.validation.Valid;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Override
    @GetMapping
    public ResponseEntity<CursorPageResponseDto<UserDto>> getUsers(
        @Valid @ModelAttribute UserGetRequest request) {

        log.info("[UserController] 계정 목록 조회 요청 - limit: {}, 정렬 기준: {}, 정렬 방향: {}",
            request.limit(), request.sortBy(), request.sortDirection());

        CursorPageResponseDto<UserDto> result = userService.getUsers(request);

        log.info("[UserController] 계정 목록 조회 성공 - {}개의 데이터, 다음 페이지 존재 여부: {}",
            result.data().size(), result.hasNext());

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Override
    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserDto> updateUserRole(
        @PathVariable UUID userId,
        @Valid @RequestBody UserRoleUpdateRequest request) {

        Role role = request.role();
        log.info("[UserController] 권한 수정 요청 - id: {} role: {}", userId, role);

        UserDto result = userService.updateUserRole(userId, role);

        log.info("UserController] 권한 수정 성공 - id: {}", result.id());

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
