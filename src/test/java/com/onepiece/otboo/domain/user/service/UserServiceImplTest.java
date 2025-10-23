package com.onepiece.otboo.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.request.UserGetRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.exception.DuplicateEmailException;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.fixture.UserDtoFixture;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import com.onepiece.otboo.domain.user.mapper.UserMapper;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Profile profile;
    private UserDto userDto;
    private UUID userId;
    private UUID profileId;
    private List<UserDto> dummyUsers;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .socialAccount(SocialAccount.builder().build())
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
            .linkedOAuthProviders(List.of())
            .locked(false)
            .build();

        dummyUsers = UserDtoFixture.createDummyUsers();
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
    void 다음_페이지가_없는_계정_목록_조회_테스트() {

        // given
        UserGetRequest request = UserGetRequest.builder()
            .limit(10)
            .sortBy(SortBy.EMAIL)
            .sortDirection(SortDirection.ASCENDING)
            .build();

        given(userRepository.findUsers(request)).willReturn(dummyUsers);
        given(userRepository.countUsers(request.emailLike(), request.roleEqual(), request.locked()))
            .willReturn(5L);

        // when
        CursorPageResponseDto<UserDto> result = userService.getUsers(request);

        // then
        assertEquals(5, result.data().size());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
        assertNull(result.nextIdAfter());
        assertEquals(5, result.totalCount());
        assertEquals(SortBy.EMAIL, result.sortBy());
        assertEquals(SortDirection.ASCENDING, result.sortDirection());
        verify(userRepository).findUsers(request);
        verify(userRepository).countUsers(request.emailLike(), request.roleEqual(),
            request.locked());
    }

    @ParameterizedTest(name = "sortBy = {0}, sortDirection = {1}")
    @CsvSource({
        "EMAIL,ASCENDING",
        "CREATED_AT,DESCENDING"
    })
    void 다음_페이지가_있는_계정_목록_조회_테스트(SortBy sortBy, SortDirection sortDirection) {

        // given
        UserGetRequest request = UserGetRequest.builder()
            .limit(2)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();

        given(userRepository.findUsers(request)).willReturn(dummyUsers);
        given(userRepository.countUsers(request.emailLike(), request.roleEqual(), request.locked()))
            .willReturn(5L);

        // when
        CursorPageResponseDto<UserDto> result = userService.getUsers(request);

        // then
        assertEquals(2, result.data().size());
        assertTrue(result.hasNext());
        assertEquals(5, result.totalCount());
        assertEquals(sortBy, result.sortBy());
        assertEquals(sortDirection, result.sortDirection());
        UserDto lastUser = result.data().get(1);
        assertEquals(lastUser.id(), result.nextIdAfter());
        if (SortBy.EMAIL.equals(sortBy)) {
            assertEquals(lastUser.email(), result.nextCursor());
        } else {
            assertEquals(lastUser.createdAt().toString(), result.nextCursor());
        }
        verify(userRepository).findUsers(request);
        verify(userRepository).countUsers(request.emailLike(), request.roleEqual(),
            request.locked());
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
        verify(jwtRegistry).blacklistAllTokens(eq(userId));
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
        verify(jwtRegistry, never()).blacklistAllTokens(eq(userId));
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
        verify(jwtRegistry).blacklistAllTokens(eq(userId));
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
        verify(jwtRegistry).blacklistAllTokens(eq(userId));
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
        verify(jwtRegistry, never()).blacklistAllTokens(eq(userId));
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
        verify(jwtRegistry, never()).blacklistAllTokens(eq(userId));
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

    @Test
    void 비밀번호_변경_성공_테스트() {

        // given
        String newPassword = "newPassword123@";

        User updatedUser = UserFixture.createUser();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willReturn(updatedUser);

        // when
        userService.updatePassword(userId, newPassword);

        // then
        verify(userRepository).save(any());
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    void 존재하지_않는_사용자의_비밀번호_변경시_예외가_발생한다() {

        // given
        UUID notExistId = UUID.randomUUID();
        String newPassword = "newPassword123@";

        given(userRepository.findById(notExistId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(
            () -> userService.updatePassword(notExistId, newPassword));

        // then
        assertThat(thrown)
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("사용자");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}