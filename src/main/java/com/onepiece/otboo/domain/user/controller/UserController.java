package com.onepiece.otboo.domain.user.controller;

import com.onepiece.otboo.domain.profile.dto.request.ProfileUpdateRequest;
import com.onepiece.otboo.domain.profile.dto.response.ProfileDto;
import com.onepiece.otboo.domain.profile.service.ProfileService;
import com.onepiece.otboo.domain.user.controller.api.UserApi;
import com.onepiece.otboo.domain.user.dto.request.ChangePasswordRequest;
import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.request.UserGetRequest;
import com.onepiece.otboo.domain.user.dto.request.UserLockUpdateRequest;
import com.onepiece.otboo.domain.user.dto.request.UserRoleUpdateRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.service.UserService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final ProfileService profileService;

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
    @GetMapping("/{userId}/profiles")
    public ResponseEntity<ProfileDto> getUserProfile(@PathVariable UUID userId) {

        log.info("[UserController] 프로필 조회 요청 - userId: {}", userId);

        ProfileDto result = profileService.getUserProfile(userId);

        log.info("[UserController] 프로필 조회 완료 - userId: {} nickname: {}", userId, result.name());

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("{userId}/role")
    public ResponseEntity<UserDto> changeRole(
        @PathVariable("userId") UUID userId,
        @Valid @RequestBody UserRoleUpdateRequest request
    ) {
        log.info("[UserController] 권한 변경 요청 - userId: {}, role: {}", userId, request.role());
        UserDto result = userService.changeRole(userId, Role.valueOf(request.role()));

        log.info("[UserController] 권한 변경 성공 - userId: {}, role: {}", userId, request.role());
        return ResponseEntity.ok(result);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/lock")
    public ResponseEntity<UserDto> updateUserLock(@PathVariable("userId") UUID userId,
        @Valid @RequestBody UserLockUpdateRequest request) {
        log.info("[UserController] 계정 잠금 상태 변경 요청 - userId: {}, locked: {}", userId,
            request.locked());

        UserDto result;
        if (request.locked()) {
            result = userService.lockUser(userId);
            log.info("[UserController] 계정 잠금 성공 - userId: {}", userId);
        } else {
            result = userService.unlockUser(userId);
            log.info("[UserController] 계정 잠금 해제 성공 - userId: {}", userId);
        }

        return ResponseEntity.ok(result);
    }

    @Override
    @PreAuthorize("#userId == principal.userId")
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, path = "/{userId}/profiles")
    public ResponseEntity<ProfileDto> updateUserProfile(
        @PathVariable UUID userId,
        @Valid @RequestPart ProfileUpdateRequest request,
        @RequestPart(value = "image", required = false) MultipartFile profileImage)
        throws IOException {

        log.info("[UserController] 프로필 업데이트 요청 - userId: {}", userId);

        ProfileDto result = profileService.update(userId, request, profileImage);

        log.info("[UserController] 프로필 업데이트 성공 - userId: {}", userId);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Override
    @PreAuthorize("#userId == principal.userId")
    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(
        @PathVariable UUID userId,
        @Valid @RequestBody ChangePasswordRequest request
    ) {

        log.info("[UserController] 비밀번호 변경 요청 - userId: {}", userId);

        userService.updatePassword(userId, request.password());

        log.info("[UserController] 비밀번호 변경 성공 - userId: {}", userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
