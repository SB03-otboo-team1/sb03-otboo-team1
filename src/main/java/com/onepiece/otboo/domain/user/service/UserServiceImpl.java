package com.onepiece.otboo.domain.user.service;

import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.request.UserGetRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.exception.DuplicateEmailException;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.mapper.UserMapper;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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
            .socialAccount(SocialAccount.builder().build())
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
        SortDirection sortDirection = userGetRequest.sortDirection();
        String emailLike = userGetRequest.emailLike();
        Role roleEqual = userGetRequest.roleEqual();
        Boolean locked = userGetRequest.locked();
        int limit = userGetRequest.limit();

        List<UserDto> users = userRepository.findUsers(userGetRequest);

        // cursor
        boolean hasNext = users.size() > limit;

        String nextCursor = null;
        UUID nextIdAfter = null;

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
            nextIdAfter = lastUser.id();
        }

        long totalCount = userRepository.countUsers(emailLike, roleEqual, locked);

        log.info("[UserService] 계정 목록 조회 완료 - {}개의 데이터, 다음 페이지 존재 여부: {}",
            totalCount, hasNext);

        return new CursorPageResponseDto<>(users, nextCursor, nextIdAfter, hasNext, totalCount,
            sortBy, sortDirection);
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
    public UserDto changeRole(UUID userId, Role role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));
        user.updateRole(role);
        userRepository.save(user);

        jwtRegistry.blacklistAllTokens(userId);

        log.info("[UserService] 사용자 권한 변경 - userId: {}, role: {}, 토큰 무효화 완료", userId, role);

        return userMapper.toDto(user, profileRepository.findByUserId(userId).orElse(null));
    }

    @Override
    @Transactional
    public UserDto lockUser(UUID userId) {
        User user = findUser(userId);
        user.updateLocked(true);
        userRepository.save(user);

        jwtRegistry.blacklistAllTokens(userId);

        log.info("[UserService] 사용자 계정 잠금 완료 - userId: {}, 토큰 무효화 완료", userId);

        Profile profile = profileRepository.findByUserId(userId)
            .orElse(null);
        return userMapper.toDto(user, profile);
    }

    @Override
    @Transactional
    public UserDto unlockUser(UUID userId) {
        User user = findUser(userId);
        user.updateLocked(false);
        userRepository.save(user);

        log.info("[UserService] 사용자 계정 잠금 해제 완료 - userId: {}", userId);

        Profile profile = profileRepository.findByUserId(userId)
            .orElse(null);
        return userMapper.toDto(user, profile);
    }

    @Override
    @Transactional
    public void updatePassword(UUID userId, String password) {
        User user = findUser(userId);

        String encodedPassword = passwordEncoder.encode(password);
        user.updatePassword(encodedPassword);

        User updatedUser = userRepository.save(user);

        log.info("[UserService] 비밀번호 변경 성공 - userId: {}", updatedUser.getId());
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));
    }

    @Transactional
    public User updateSocialAccountAndProfile(User user, Provider provider, String providerUserId,
        String nickname, String profileImageUrl) {
        if (user.isLocked()) {
            throw new OAuth2AuthenticationException("접근 거부되었습니다.");
        }
        var socialAccount = user.getSocialAccount();
        if (socialAccount == null || !socialAccount.isValid() || !socialAccount.isSameProviderAndId(
            provider, providerUserId)) {
            user.linkSocialAccount(provider, providerUserId);
            user = userRepository.save(user);
        }
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            profile = Profile.builder()
                .user(user)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();
            profileRepository.save(profile);
        } else {
            if ((profile.getNickname() == null || profile.getNickname().isBlank())
                && nickname != null) {
                profile.updateNickname(nickname);
            }
            if ((profile.getProfileImageUrl() == null || profile.getProfileImageUrl().isBlank())
                && profileImageUrl != null) {
                profile.updateProfileImageUrl(profileImageUrl);
            }
        }
        return user;
    }

    @Transactional
    public User createSocialUserAndProfile(Provider provider, String providerUserId, String email,
        String nickname, String profileImageUrl) {
        User newUser = User.builder()
            .socialAccount(SocialAccount.builder()
                .provider(provider)
                .providerUserId(providerUserId)
                .build())
            .email(email)
            .password("")
            .role(Role.USER)
            .locked(false)
            .build();
        newUser = userRepository.save(newUser);

        Profile profile = Profile.builder()
            .user(newUser)
            .nickname(nickname)
            .profileImageUrl(profileImageUrl)
            .build();
        profileRepository.save(profile);
        return newUser;
    }
}
