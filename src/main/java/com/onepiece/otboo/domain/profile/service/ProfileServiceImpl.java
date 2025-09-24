package com.onepiece.otboo.domain.profile.service;

import com.onepiece.otboo.domain.profile.dto.request.ProfileUpdateRequest;
import com.onepiece.otboo.domain.profile.dto.response.ProfileDto;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.exception.ProfileNotFoundException;
import com.onepiece.otboo.domain.profile.mapper.ProfileMapper;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional(readOnly = true)
    public ProfileDto getUserProfile(UUID userId) {
        User user = findUser(userId);
        Profile profile = findProfile(userId);

        log.info("[ProfileService] 프로필 조회 완료 - userId: {}", userId);

        return profileMapper.toDto(user, profile);
    }

    @Override
    @Transactional
    public ProfileDto update(UUID userId, ProfileUpdateRequest request,
        MultipartFile profileImage) {
        User user = findUser(userId);
        Profile profile = findProfile(userId);

        Profile updatedProfile = profileRepository.save(profile);

        log.info("[ProfileService] 프로필 업데이트 성공 - userId: {}", userId);

        return profileMapper.toDto(user, updatedProfile);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));
    }

    private Profile findProfile(UUID userId) {
        return profileRepository.findByUserId(userId)
            .orElseThrow(() -> new ProfileNotFoundException(userId));
    }
}
