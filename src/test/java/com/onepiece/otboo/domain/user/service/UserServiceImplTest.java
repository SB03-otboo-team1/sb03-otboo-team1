package com.onepiece.otboo.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

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
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtRegistry jwtRegistry;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Profile profile;
    private UserDto userDto;
    private UUID userId;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .provider(Provider.LOCAL)
            .email("test@test.com")
            .password("encodedPassword")
            .locked(false)
            .role(Role.USER)
            .build();

        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();

        ReflectionTestUtils.setField(user, "id", userId);

        profile = Profile.builder()
            .user(user)
            .nickname("test")
            .build();

        ReflectionTestUtils.setField(profile, "id", profileId);

        userDto = UserDto.builder()
            .id(userId)
            .createdAt(Instant.now())
            .name("test")
            .email("test@test.com")
            .role(Role.USER)
            .linkedOAuthProviders(List.of(Provider.LOCAL))
            .locked(false)
            .build();
    }

    @Test
    void 회원가입_성공시_사용자_정보와_프로필이_저장된다() {
        // given
        UserCreateRequest request = new UserCreateRequest("test", "test@test.com",
            "test1234@");

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(user);
        given(userMapper.toDto(any(User.class), any(Profile.class))).willReturn(userDto);
        given(profileRepository.save(any(Profile.class))).willReturn(profile);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

        // when
        UserDto result = userService.create(request);

        // then
        assertNotNull(result);
        assertEquals(userDto, result);
        verify(userRepository).existsByEmail("test@test.com");
        verify(passwordEncoder).encode(anyString());
        verify(userMapper).toDto(user, profile);
        verify(userRepository).save(any(User.class));
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void 중복_이메일로_회원가입시_사용자_등록에_실패한다() {

        // given
        String existsEmail = "duplicate@duplicate.com";

        UserCreateRequest request = new UserCreateRequest("test", existsEmail, "test1234@");

        given(userRepository.existsByEmail(existsEmail)).willReturn(true);

        // when
        Throwable thrown = catchThrowable(() -> userService.create(request));

        // then
        assertThat(thrown)
            .isInstanceOf(DuplicateEmailException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void 권한변경_성공시_역할이_변경된다() {
        // given
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(user);

        // when
        userService.changeRole(userId, Role.ADMIN);

        // then
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(jwtRegistry).invalidateAllTokens(eq(userId), any(Instant.class));
    }

    @Test
    void 존재하지않는사용자_권한변경시_예외발생() {
        // given
        UUID notExistId = UUID.randomUUID();
        given(userRepository.findById(notExistId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> userService.changeRole(notExistId, Role.ADMIN));

        // then
        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
        verify(userRepository).findById(notExistId);
        verify(userRepository, never()).save(any());
        verify(jwtRegistry, never()).invalidateAllTokens(any(UUID.class), any(Instant.class));
    }

    @Test
    void 권한변경_시_JWT_토큰_무효화가_호출된다() {
        // given
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(user);
        Role oldRole = user.getRole();

        // when
        userService.changeRole(userId, Role.ADMIN);

        // then
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.getRole()).isNotEqualTo(oldRole);
        verify(jwtRegistry).invalidateAllTokens(eq(userId), any(Instant.class));
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    void 계정잠금_성공시_계정이_잠기고_토큰이_무효화된다() {
        // given
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(user);

        // when
        userService.lockUser(userId);

        // then
        assertThat(user.isLocked()).isTrue();
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(jwtRegistry).invalidateAllTokens(eq(userId), any(Instant.class));
    }

    @Test
    void 존재하지않는사용자_계정잠금시_예외발생() {
        // given
        UUID notExistId = UUID.randomUUID();
        given(userRepository.findById(notExistId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> userService.lockUser(notExistId));

        // then
        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
        verify(userRepository).findById(notExistId);
        verify(userRepository, never()).save(any());
        verify(jwtRegistry, never()).invalidateAllTokens(any(UUID.class), any(Instant.class));
    }

    @Test
    void 계정잠금해제_성공시_계정이_해제된다() {
        // given
        user.updateLocked(true);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(user);

        // when
        userService.unlockUser(userId);

        // then
        assertThat(user.isLocked()).isFalse();
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        // 계정 해제 시에는 토큰 무효화 불필요
        verify(jwtRegistry, never()).invalidateAllTokens(any(UUID.class), any(Instant.class));
    }

    @Test
    void 존재하지않는사용자_계정잠금해제시_예외발생() {
        // given
        UUID notExistId = UUID.randomUUID();
        given(userRepository.findById(notExistId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> userService.unlockUser(notExistId));

        // then
        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
        verify(userRepository).findById(notExistId);
        verify(userRepository, never()).save(any());
    }
}