package com.onepiece.otboo.domain.user.controller;

import com.onepiece.otboo.domain.user.controller.api.UserApi;
import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.request.UserLockUpdateRequest;
import com.onepiece.otboo.domain.user.dto.request.UserRoleUpdateRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("{userId}/role")
    public ResponseEntity<Void> changeRole(
        @PathVariable("userId") String userId,
        UserRoleUpdateRequest request
    ) {
        log.info("[UserController] 권한 변경 요청 - userId: {}, role: {}", userId, request.role());
        userService.changeRole(UUID.fromString(userId), Role.valueOf(request.role()));

        log.info("[UserController] 권한 변경 성공 - userId: {}, role: {}", userId, request.role());
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/lock")
    public ResponseEntity<UserDto> updateUserLock(@PathVariable("userId") String userId,
        @Valid @RequestBody UserLockUpdateRequest request) {
        log.info("[UserController] 계정 잠금 상태 변경 요청 - userId: {}, locked: {}", userId,
            request.locked());

        UserDto result;
        if (request.locked()) {
            result = userService.lockUser(UUID.fromString(userId));
            log.info("[UserController] 계정 잠금 성공 - userId: {}", userId);
        } else {
            result = userService.unlockUser(UUID.fromString(userId));
            log.info("[UserController] 계정 잠금 해제 성공 - userId: {}", userId);
        }

        return ResponseEntity.ok(result);
    }
}
