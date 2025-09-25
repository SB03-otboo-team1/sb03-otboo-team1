package com.onepiece.otboo.domain.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.location.service.LocationPersistenceService;
import com.onepiece.otboo.domain.profile.dto.response.ProfileDto;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.exception.ProfileNotFoundException;
import com.onepiece.otboo.domain.profile.fixture.ProfileDtoFixture;
import com.onepiece.otboo.domain.profile.fixture.ProfileFixture;
import com.onepiece.otboo.domain.profile.mapper.ProfileMapper;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.storage.FileStorage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private LocationPersistenceService locationPersistenceService;

    @Mock
    private FileStorage storage;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private UUID userId;
    private User user;
    private Profile profile;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = UserFixture.createUser("test@test.com");
        ReflectionTestUtils.setField(user, "id", userId);

        profile = ProfileFixture.createProfile(user);
    }

    @Test
    void 프로필_조회_성공_테스트() {

        // given
        ProfileDto profileDto = ProfileDtoFixture.createProfile(userId);

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(profileRepository.findByUserId(any())).willReturn(Optional.of(profile));
        given(profileMapper.toDto(any(User.class), any(Profile.class), any(FileStorage.class))).willReturn(profileDto);

        // when
        ProfileDto result = profileService.getUserProfile(userId);

        // then
        assertNotNull(result);
        assertEquals(userId, result.userId());
        assertEquals("test", result.name());
        assertEquals("1999-07-02", result.birthDate().toString());
        verify(userRepository).findById(userId);
        verify(profileRepository).findByUserId(userId);
        verify(profileMapper).toDto(user, profile, storage);
    }

    @Test
    void 존재하지_않는_사용자의_프로필_조회_요청시_예외가_발생한다() {

        // given
        UUID notExistId = UUID.randomUUID();

        given(userRepository.findById(notExistId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> profileService.getUserProfile(notExistId));

        // then
        assertThat(thrown)
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("사용자");
        verify(profileMapper, never()).toDto(any(), any(), any());
    }

    @Test
    void 특정_사용자의_프로필이_존재하지_않을_때_예외가_발생한다() {

        // given
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(profileRepository.findByUserId(any())).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> profileService.getUserProfile(userId));

        // then
        assertThat(thrown)
            .isInstanceOf(ProfileNotFoundException.class)
            .hasMessageContaining("프로필");
        verify(profileMapper, never()).toDto(any(), any(), any());
    }
}