package com.onepiece.otboo.domain.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.fixture.LocationFixture;
import com.onepiece.otboo.domain.location.service.LocationPersistenceService;
import com.onepiece.otboo.domain.profile.dto.request.ProfileUpdateRequest;
import com.onepiece.otboo.domain.profile.dto.response.ProfileDto;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.enums.Gender;
import com.onepiece.otboo.domain.profile.exception.ProfileNotFoundException;
import com.onepiece.otboo.domain.profile.fixture.ProfileDtoFixture;
import com.onepiece.otboo.domain.profile.fixture.ProfileFixture;
import com.onepiece.otboo.domain.profile.mapper.ProfileMapper;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import com.onepiece.otboo.global.storage.FileStorage;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
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

    @Mock
    private ProfileImageAsyncService profileImageAsyncService;

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

        stubUserFound();
        stubProfileFound();
        stubMapper(profileDto);

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
    void 존재하지_않는_사용자의_프로필_조회_요청시_예외() {

        // given
        UUID notExistId = UUID.randomUUID();
        stubUserNotFound(notExistId);

        // when
        Throwable thrown = catchThrowable(() -> profileService.getUserProfile(notExistId));

        // then
        assertThat(thrown).isInstanceOf(UserNotFoundException.class).hasMessageContaining("사용자");
        verify(profileMapper, never()).toDto(any(), any(), any());
    }

    @Test
    void 특정_사용자의_프로필이_없으면_예외() {

        // given
        stubUserFound();
        given(profileRepository.findByUserId(any())).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> profileService.getUserProfile(userId));

        // then
        assertThat(thrown).isInstanceOf(ProfileNotFoundException.class).hasMessageContaining("프로필");
        verify(profileMapper, never()).toDto(any(), any(), any());
    }

    @Test
    void 프로필_업데이트_성공_테스트() throws Exception {

        // given
        ProfileUpdateRequest request = req(Gender.MALE, BIRTH, 2);
        MockMultipartFile newProfile = new MockMultipartFile("profileImage", "profile.png",
            "image/png", "dummy".getBytes());
        Location location = LocationFixture.createLocation();

        Profile updated = ProfileFixture.createProfile(user, NAME, "profile.png", Gender.MALE,
            BIRTH, 2, location);
        ProfileDto dto = dto(userId, NAME, Gender.MALE, BIRTH, 2, null);

        stubUserFound();
        stubProfileFound();
        stubSaveReturns(updated);
        stubMapper(dto);

        // when
        ProfileDto result = profileService.update(userId, request, newProfile);

        // then
        assertNotNull(result);
        assertEquals(userId, result.userId());
        assertEquals(NAME, result.name());
        assertEquals(Gender.MALE, result.gender());
        assertEquals(BIRTH, result.birthDate());
        assertEquals(2, result.temperatureSensitivity());
        verify(profileImageAsyncService).replaceProfileImageAsync(eq(userId), any(), any(), any());
    }

    @Test
    void Location_이미_존재하면_재사용() throws Exception {

        // given
        WeatherAPILocation wloc = locDto();
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
            .name(NAME).gender(Gender.MALE).birthDate(BIRTH).temperatureSensitivity(3)
            .location(wloc).build();
        Location existing = locEntityFromDto(wloc);
        Profile updated = ProfileFixture.createProfile(user, NAME, "profile.png", Gender.MALE,
            BIRTH, 2, existing);
        ProfileDto out = dto(userId, null, Gender.MALE, BIRTH, 2, wloc);

        stubUserFound();
        stubProfileFound();
        given(locationPersistenceService.findByLatitudeAndLongitude(anyDouble(), anyDouble()))
            .willReturn(Optional.of(existing));
        stubSaveReturns(updated);
        stubMapper(out);

        // when
        ProfileDto dto = profileService.update(userId, request, null);

        // then
        assertEquals(wloc, dto.location());
        verify(locationPersistenceService).findByLatitudeAndLongitude(anyDouble(), anyDouble());
        verify(locationPersistenceService, never()).save(any());
    }

    @Test
    void Location_없으면_새로_생성() throws Exception {

        // given
        WeatherAPILocation wloc = locDto();
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
            .name(NAME).gender(Gender.MALE).birthDate(BIRTH).temperatureSensitivity(3)
            .location(wloc).build();
        Location created = locEntityFromDto(wloc);
        Profile updated = ProfileFixture.createProfile(user, NAME, "profile.png", Gender.MALE,
            BIRTH, 2, created);
        ProfileDto out = dto(userId, null, Gender.MALE, BIRTH, 2, wloc);

        stubUserFound();
        stubProfileFound();
        given(locationPersistenceService.findByLatitudeAndLongitude(anyDouble(), anyDouble()))
            .willReturn(Optional.empty());
        stubSaveReturns(updated);
        stubMapper(out);

        // when
        ProfileDto dto = profileService.update(userId, request, null);

        // then
        assertEquals(wloc, dto.location());
        verify(locationPersistenceService).findByLatitudeAndLongitude(anyDouble(), anyDouble());
        verify(locationPersistenceService).save(any());
    }

    @Test
    void 존재하지_않는_사용자_수정_요청시_예외() {

        // given
        UUID notExistId = UUID.randomUUID();
        ProfileUpdateRequest request = req(Gender.MALE, BIRTH, 2);

        stubUserNotFound(notExistId);

        // when
        Throwable thrown = catchThrowable(() -> profileService.update(notExistId, request, null));

        // then
        assertThat(thrown).isInstanceOf(UserNotFoundException.class).hasMessageContaining("사용자");
        verify(profileMapper, never()).toDto(any(), any(), any());
    }

    @Test
    void 존재하지_않는_프로필_수정_요청시_예외() {

        // given
        ProfileUpdateRequest request = req(Gender.MALE, BIRTH, 2);

        stubUserFound();
        stubProfileNotFound(userId);

        // when
        Throwable thrown = catchThrowable(() -> profileService.update(userId, request, null));

        // then
        assertThat(thrown).isInstanceOf(ProfileNotFoundException.class).hasMessageContaining("프로필");
        verify(profileMapper, never()).toDto(any(), any(), any());
    }

    @Test
    void 이미지_null_이면_변경없음() throws Exception {

        // given
        ProfileDto out = dto(userId, NAME, Gender.MALE, BIRTH, 2, null);

        stubUserFound();
        stubProfileFound();
        stubSaveReturns(profile);
        stubMapper(out);

        // when
        profileService.update(userId, req(Gender.MALE, BIRTH, 2), null);

        // then
        verify(storage, never()).uploadFile(any(), any());
        verify(storage, never()).deleteFile(anyString());
    }

    @Test
    void 이미지_빈파일_이면_기존삭제_및_null() throws Exception {

        // given
        MockMultipartFile empty = new MockMultipartFile("profileImage", "empty.png", "image/png",
            new byte[0]);
        ProfileDto out = dto(userId, NAME, Gender.MALE, BIRTH, 2, null);

        stubUserFound();
        stubProfileFound();
        stubSaveReturns(profile);
        stubMapper(out);

        // when
        profileService.update(userId, req(Gender.MALE, BIRTH, 2), empty);

        // then
        verify(storage).deleteFile(anyString());
        verify(storage, never()).uploadFile(any(), any());
    }

    @Test
    void 이미지_신규파일_이면_업로드_후_기존삭제() throws Exception {

        // given
        MockMultipartFile file = new MockMultipartFile("profileImage", "new.png", "image/png",
            "dummy".getBytes());
        Profile updated = ProfileFixture.createProfile(user);
        ReflectionTestUtils.setField(updated, "profileImageUrl", "new-key");
        ProfileDto out = dto(userId, NAME, Gender.MALE, BIRTH, 2, null);

        stubUserFound();
        stubProfileFound();
        stubSaveReturns(updated);
        stubMapper(out);

        // when
        profileService.update(userId, req(Gender.MALE, BIRTH, 2), file);

        // then
        verify(profileImageAsyncService).replaceProfileImageAsync(eq(userId), any(), any(),
            any());
    }

    private static final LocalDate BIRTH = LocalDate.of(1999, 7, 2);
    private static final String NAME = "한동우";

    private ProfileUpdateRequest req(Gender gender, LocalDate birth, Integer temp) {
        return ProfileDtoFixture.createUpdateRequest(ProfileServiceImplTest.NAME, gender, birth,
            temp);
    }

    private WeatherAPILocation locDto() {
        return new WeatherAPILocation(
            37.43751196107601, 126.8054724084087, 57, 124, List.of("경기도", "시흥시", "은행동")
        );
    }

    private Location locEntityFromDto(WeatherAPILocation dto) {
        Location l = Location.builder()
            .latitude(dto.latitude())
            .longitude(dto.longitude())
            .xCoordinate(dto.x())
            .yCoordinate(dto.y())
            .locationNames(String.join(",", dto.locationNames()))
            .build();
        ReflectionTestUtils.setField(l, "id", UUID.randomUUID());
        return l;
    }

    private ProfileDto dto(UUID id, String name, Gender gender, LocalDate birth, Integer temp,
        WeatherAPILocation loc) {
        return ProfileDto.builder()
            .userId(id).name(name).gender(gender).birthDate(birth)
            .temperatureSensitivity(temp).location(loc).build();
    }

    private void stubUserFound() {
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
    }

    private void stubUserNotFound(UUID id) {
        given(userRepository.findById(id)).willReturn(Optional.empty());
    }

    private void stubProfileFound() {
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
    }

    private void stubProfileNotFound(UUID id) {
        given(profileRepository.findByUserId(id)).willReturn(Optional.empty());
    }

    private void stubMapper(ProfileDto dto) {
        given(profileMapper.toDto(any(User.class), any(Profile.class), any(FileStorage.class)))
            .willReturn(dto);
    }

    private void stubSaveReturns(Profile updated) {
        given(profileRepository.save(any(Profile.class))).willReturn(updated);
    }
}
