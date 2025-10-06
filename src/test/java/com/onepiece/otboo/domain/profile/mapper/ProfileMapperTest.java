package com.onepiece.otboo.domain.profile.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.fixture.LocationFixture;
import com.onepiece.otboo.domain.profile.dto.response.ProfileDto;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.fixture.ProfileFixture;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.global.storage.S3Storage;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProfileMapperTest {

    private final ProfileMapper mapper = Mappers.getMapper(ProfileMapper.class);

    @Test
    void S3Storage_컨텍스트가_주어지면_profileImageUrl에_presigned_url이_적용된다() {

        // given
        UUID userId = UUID.randomUUID();
        User user = UserFixture.createUser();
        ReflectionTestUtils.setField(user, "id", userId);

        Profile profile = ProfileFixture.createProfile(user);
        profile.updateProfileImageUrl("profile/image.png"); // S3 key로 세팅
        Location location = LocationFixture.createLocation();
        profile.updateLocation(location);

        S3Storage s3 = mock(S3Storage.class);
        given(s3.generatePresignedUrl("profile/image.png"))
            .willReturn("https://cdn.example.com/profile/image.jpg?sig=xxx");

        // when
        ProfileDto result = mapper.toDto(user, profile, s3);

        // then
        assertEquals(userId, result.userId());
        assertEquals("test", result.name());
        assertEquals(LocalDate.of(1999, 7, 2), result.birthDate());
        assertNotNull(result.location());
        assertEquals(5, result.temperatureSensitivity());
        assertEquals("https://cdn.example.com/profile/image.jpg?sig=xxx", result.profileImageUrl());
        verify(s3).generatePresignedUrl("profile/image.png");
    }

    @Test
    void 프로필_이미지가_없다면_URL_변환이_되지않는다() {

        // given
        UUID userId = UUID.randomUUID();
        User user = UserFixture.createUser();
        ReflectionTestUtils.setField(user, "id", userId);

        Profile profile = ProfileFixture.createProfile(user);
        profile.updateProfileImageUrl(null);
        Location location = LocationFixture.createLocation();
        profile.updateLocation(location);

        S3Storage s3 = mock(S3Storage.class);

        // when
        ProfileDto dto = mapper.toDto(user, profile, s3);

        // then
        assertNull(dto.profileImageUrl());
        verify(s3, never()).generatePresignedUrl(anyString());
    }

    @Test
    void S3Storage_컨텍스트가_아니라면_key를_그대로_전달한다() {

        // given
        UUID userId = UUID.randomUUID();
        User user = UserFixture.createUser();
        ReflectionTestUtils.setField(user, "id", userId);

        Profile profile = ProfileFixture.createProfile();

        FileStorage localStorage = mock(FileStorage.class);

        // when
        ProfileDto result = mapper.toDto(user, profile, localStorage);

        // then
        assertEquals("https://example.com/image.png", result.profileImageUrl());
        assertNull(result.location());
    }

    @Test
    void User가_null이면_null을_반환한다() {

        // given
        S3Storage s3 = mock(S3Storage.class);

        // when
        ProfileDto result = mapper.toDto(null, null, s3);

        // then
        assertNull(result);
        verify(s3, never()).generatePresignedUrl(anyString());
    }
}