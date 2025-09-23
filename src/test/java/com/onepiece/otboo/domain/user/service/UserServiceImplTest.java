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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
import com.onepiece.otboo.domain.user.fixture.UserDtoFixture;
import com.onepiece.otboo.domain.user.mapper.UserMapper;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
            .sortBy("email")
            .sortDirection("ASCENDING")
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
        assertEquals("email", result.sortBy());
        assertEquals("ASCENDING", result.sortDirection());
        verify(userRepository).findUsers(request);
        verify(userRepository).countUsers(request.emailLike(), request.roleEqual(),
            request.locked());
    }

    @ParameterizedTest(name = "sortBy = {0}, sortDirection = {1}")
    @CsvSource({
        "email,ASCENDING",
        "createdAt,DESCENDING"
    })
    void 다음_페이지가_있는_계정_목록_조회_테스트(String sortBy, String sortDirection) {

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
        assertEquals(lastUser.id().toString(), result.nextIdAfter());
        if ("email".equals(sortBy)) {
            assertEquals(lastUser.email(), result.nextCursor());
        } else {
            assertEquals(lastUser.createdAt().toString(), result.nextCursor());
        }
        verify(userRepository).findUsers(request);
        verify(userRepository).countUsers(request.emailLike(), request.roleEqual(),
            request.locked());
    }

    @Test
    void USER_권한을_가진_사용자가_권한_수정_요청시_AccessDeniedException이_발생한다() {

        // given
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("user@test.com", "pwd1234@",
                List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        UserServiceImpl proxy = secured(userService);
        UUID userId = UUID.randomUUID();

        // when
        Throwable thrown = catchThrowable(() -> proxy.updateUserRole(userId, Role.ADMIN));

        // then
        assertThat(thrown)
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ADMIN_권한을_가진_사용자가_권한_수정_요청시_정상적으로_수정된다() {

        // given
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("admin@test.com", "pwd1234@",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        UserServiceImpl proxy = secured(userService);

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(profileRepository.findByUserId(any())).willReturn(Optional.of(profile));
        given(userRepository.save(any())).willReturn(user);
        given(userMapper.toDto(any(User.class), any(Profile.class))).willReturn(userDto);

        // when
        UserDto result = proxy.updateUserRole(userId, Role.ADMIN);

        // then
        assertNotNull(result);
        assertEquals(userId, result.id());
    }

    private UserServiceImpl secured(UserServiceImpl target) {
        AuthorizationManagerBeforeMethodInterceptor interceptor =
            AuthorizationManagerBeforeMethodInterceptor.preAuthorize();

        ProxyFactory pf = new ProxyFactory(target);
        pf.setProxyTargetClass(true);
        pf.addAdvice(interceptor);
        return (UserServiceImpl) pf.getProxy();
    }

    @Test
    void 존재하지_않는_사용자에_대해_권한_수정_요청시_404가_발생한다() {

        // given
        UUID notExistId = UUID.randomUUID();

        given(userRepository.findById(any())).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> userService.updateUserRole(notExistId, Role.ADMIN));

        // then
        assertThat(thrown)
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("사용자");
        verify(userRepository, never()).save(any());
    }

    @Test
    void 권한_수정시_사용자의_프로필이_존재하지_않으면_404가_발생한다() {

        // given
        UUID notExistId = UUID.randomUUID();

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(profileRepository.findByUserId(any())).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> userService.updateUserRole(notExistId, Role.ADMIN));

        // then
        assertThat(thrown)
            .isInstanceOf(ProfileNotFoundException.class)
            .hasMessageContaining("프로필");
        verify(userRepository, never()).save(any());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}