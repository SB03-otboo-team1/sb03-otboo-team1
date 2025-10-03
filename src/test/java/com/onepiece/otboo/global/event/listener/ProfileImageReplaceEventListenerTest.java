package com.onepiece.otboo.global.event.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.global.event.event.ProfileImageReplaceEvent;
import com.onepiece.otboo.global.storage.S3Storage;
import com.onepiece.otboo.global.storage.payload.UploadPayload;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileImageReplaceEventListenerTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private S3Storage storage;

    @InjectMocks
    ProfileImageReplaceEventListener listener;

    private UploadPayload payload;

    private ProfileImageReplaceEvent event(UUID userId, String prefix, UploadPayload payload,
        String oldKey) {
        return new ProfileImageReplaceEvent(userId, prefix, payload, oldKey);
    }

    @BeforeEach
    void setUp() {
        payload = new UploadPayload(
            "image-bytes".getBytes(),
            "new.jpg",
            "image/jpg",
            "image-bytes".getBytes().length
        );
    }

    @Test
    void 새로운_이미지_업로드_테스트() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        String prefix = "profiles/";
        String oldKey = "profiles/old.jpg";
        String newKey = "profiles/new.jpg";

        Profile profile = mock(Profile.class);

        given(storage.uploadBytes(prefix, payload)).willReturn(newKey);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

        // when
        assertDoesNotThrow(() -> listener.on(event(userId, prefix, payload, oldKey)));

        // then
        verify(storage).uploadBytes(prefix, payload);
        verify(profileRepository).findByUserId(userId);
        verify(profile).updateProfileImageUrl(newKey);
        verify(storage).deleteFile(oldKey);
        verifyNoMoreInteractions(storage, profileRepository, profile);
    }

    @Test
    void 기존_키가_null이면_삭제를_생략한다() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        String prefix = "profiles/";
        UploadPayload payload = new UploadPayload(
            "image-bytes".getBytes(),
            "new.jpg",
            "image/jpg",
            "image-bytes".getBytes().length
        );
        String newKey = "profiles/same.jpg";
        Profile profile = mock(Profile.class);

        given(storage.uploadBytes(prefix, payload)).willReturn(newKey);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

        //when
        assertDoesNotThrow(() -> listener.on(event(userId, prefix, payload, null)));

        // then
        verify(storage).uploadBytes(prefix, payload);
        verify(profileRepository).findByUserId(userId);
        verify(profile).updateProfileImageUrl(newKey);
        verify(storage, never()).deleteFile(any());
    }

    @Test
    void 기존_키가_새로운키와_같으면_삭제를_생략한다() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        String prefix = "profiles/";
        String newKey = "profiles/same.jpg";
        Profile profile = mock(Profile.class);

        given(storage.uploadBytes(prefix, payload)).willReturn(newKey);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

        // when
        assertDoesNotThrow(() -> listener.on(event(userId, prefix, payload, newKey)));

        // then
        verify(storage).uploadBytes(prefix, payload);
        verify(profileRepository).findByUserId(userId);
        verify(profile).updateProfileImageUrl(newKey);
        verify(storage, never()).deleteFile(any());
    }

    @Test
    @DisplayName("프로필 미존재 시: 내부에서 캐치(로그만)하고 예외 전파 없음")
    void 프로필이_존재하지_않을_때_로그_테스트() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        String prefix = "profiles/";

        given(storage.uploadBytes(prefix, payload)).willReturn("profiles/new.jpg");
        given(profileRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when
        assertDoesNotThrow(() -> listener.on(event(userId, prefix, payload, "profiles/old.jpg")));

        // then
        verify(storage).uploadBytes(prefix, payload);
        verify(profileRepository).findByUserId(userId);
        // 프로필이 없으므로 이후 동작 없음
        verify(storage, never()).deleteFile(any());
    }

    @Test
    void 업로드_실패시_로그_테스트() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        String prefix = "profiles/";

        given(storage.uploadBytes(prefix, payload)).willThrow(new RuntimeException("S3 error"));

        // when
        assertDoesNotThrow(() -> listener.on(event(userId, prefix, payload, "profiles/old.jpg")));

        // then
        verify(storage).uploadBytes(prefix, payload);
        verifyNoInteractions(profileRepository);
    }

    @Test
    void 이전_키_삭제_실패_시_로그_테스트() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String prefix = "profiles/";
        String oldKey = "profiles/old.jpg";
        String newKey = "profiles/new.jpg";

        Profile profile = mock(Profile.class);

        given(storage.uploadBytes(prefix, payload)).willReturn(newKey);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
        doThrow(new RuntimeException("delete failed")).when(storage).deleteFile(oldKey);

        // when
        assertDoesNotThrow(() -> listener.on(event(userId, prefix, payload, oldKey)));

        // then
        verify(storage).uploadBytes(prefix, payload);
        verify(profileRepository).findByUserId(userId);
        verify(profile).updateProfileImageUrl(newKey);
        verify(storage).deleteFile(oldKey);
    }
}