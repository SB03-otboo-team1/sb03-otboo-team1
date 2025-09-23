package com.onepiece.otboo.domain.user.service;

import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.exception.ProfileNotFoundException;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.request.UserGetRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.exception.DuplicateEmailException;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.mapper.UserMapper;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDto create(UserCreateRequest userCreateRequest) {

        String email = userCreateRequest.email();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(ErrorCode.DUPLICATE_EMAIL, Map.of("email", email));
        }

        log.info("[UserService] 사용자 등록 요청 - name: {}, email: {}", userCreateRequest.name(),
            userCreateRequest.email());

        String encodedPassword = passwordEncoder.encode(userCreateRequest.password());

        User user = User.builder()
            .provider(Provider.LOCAL)
            .email(email)
            .password(encodedPassword)
            .role(Role.USER)
            .locked(false)
            .build();

        User savedUser = userRepository.save(user);
        Profile profile = createProfile(savedUser, userCreateRequest.name());

        log.info("[UserService] 사용자 및 프로필 등록 완료 - userId: {}, profileId: {}",
            savedUser.getId(), profile.getId());

        return userMapper.toDto(savedUser, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseDto<UserDto> getUsers(UserGetRequest userGetRequest) {
        String sortBy = userGetRequest.sortBy();
        String sortDirection = userGetRequest.sortDirection();
        String emailLike = userGetRequest.emailLike();
        Role roleEqual = userGetRequest.roleEqual();
        Boolean locked = userGetRequest.locked();
        int limit = userGetRequest.limit();

        List<UserDto> users = userRepository.findUsers(userGetRequest);

        // cursor
        boolean hasNext = users.size() > limit;

        String nextCursor = null;
        String nextIdAfter = null;

        if (hasNext) {
            users = users.subList(0, limit);
            UserDto lastUser = users.get(limit - 1);
            switch (userGetRequest.sortByEnum()) {
                case CREATED_AT -> {
                    nextCursor = lastUser.createdAt().toString();
                }
                case EMAIL -> {
                    nextCursor = lastUser.email();
                }
            }
            nextIdAfter = lastUser.id().toString();
        }

        long totalCount = userRepository.countUsers(emailLike, roleEqual, locked);

        log.info("[UserService] 계정 목록 조회 완료 - {}개의 데이터, 다음 페이지 존재 여부: {}",
            totalCount, hasNext);

        return new CursorPageResponseDto<>(users, nextCursor, nextIdAfter, hasNext, totalCount,
            sortBy, sortDirection);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateUserRole(UUID userId, Role role) {
        User user = findUser(userId);
        Profile profile = findProfile(userId);

        user.updateRole(role);
        User updatedUser = userRepository.save(user);

        log.info("[UserService] 권한 수정 실행 완료 - id: {}", updatedUser.getId());

        return userMapper.toDto(updatedUser, profile);
    }

    private Profile createProfile(User user, String name) {
        Profile profile = Profile.builder()
            .user(user)
            .nickname(name)
            .build();

        return profileRepository.save(profile);
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> UserNotFoundException.byId(id));
    }

    private Profile findProfile(UUID id) {
        return profileRepository.findByUserId(id)
            .orElseThrow(() -> new ProfileNotFoundException(id));
    }
}
