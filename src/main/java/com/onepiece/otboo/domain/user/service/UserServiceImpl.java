package com.onepiece.otboo.domain.user.service;

import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.exception.DuplicateEmailException;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.mapper.UserMapper;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final JwtRegistry jwtRegistry;

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

    private Profile createProfile(User user, String name) {
        Profile profile = Profile.builder()
            .user(user)
            .nickname(name)
            .build();

        return profileRepository.save(profile);
    }

    @Override
    @Transactional
    public void changeRole(UUID userId, Role role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));
        user.updateRole(role);
        userRepository.save(user);

        jwtRegistry.invalidateAllTokens(userId, Instant.now());

        log.info("[UserService] 사용자 권한 변경 - userId: {}, role: {}, 토큰 무효화 완료", userId, role);
    }

    @Override
    @Transactional
    public UserDto lockUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));
        user.updateLocked(true);
        userRepository.save(user);

        jwtRegistry.invalidateAllTokens(userId, Instant.now());

        log.info("[UserService] 사용자 계정 잠금 완료 - userId: {}, 토큰 무효화 완료", userId);

        Profile profile = profileRepository.findByUserId(userId)
            .orElse(null);
        return userMapper.toDto(user, profile);
    }

    @Override
    @Transactional
    public UserDto unlockUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));
        user.updateLocked(false);
        userRepository.save(user);

        log.info("[UserService] 사용자 계정 잠금 해제 완료 - userId: {}", userId);

        Profile profile = profileRepository.findByUserId(userId)
            .orElse(null);
        return userMapper.toDto(user, profile);
    }
}
