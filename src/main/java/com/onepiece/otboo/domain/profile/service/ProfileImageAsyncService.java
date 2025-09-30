package com.onepiece.otboo.domain.profile.service;

import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.exception.ProfileNotFoundException;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.global.storage.S3Storage;
import com.onepiece.otboo.global.storage.payload.UploadPayload;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileImageAsyncService {

    private final ProfileRepository profileRepository;
    private final S3Storage storage;

    @Async("binaryContentExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void replaceProfileImageAsync(UUID userId, String prefix, UploadPayload payload,
        String oldKey) {
        try {
            // 업로드
            String newKey = storage.uploadBytes(prefix, payload);

            // 프로필 재조회 & 반영
            Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
            profile.updateProfileImageUrl(newKey);

            // 이전 키 삭제
            if (oldKey != null && !oldKey.equals(newKey)) {
                try {
                    storage.deleteFile(oldKey);
                } catch (Exception e) {
                    log.warn("[ProfileImageAsync] 이전 이미지 삭제 실패 - key: {}", oldKey, e);
                }
            }

            log.info("[ProfileImageAsync] 비동기 업로드 완료 - userId: {}, newKey: {}", userId, newKey);
        } catch (IOException e) {
            log.error("[ProfileImageAsync] 비동기 업로드 실패(IO) - userId: {}", userId, e);
        } catch (Exception e) {
            log.error("[ProfileImageAsync] 비동기 업로드 실패 - userId: {}", userId, e);
        }
    }
}