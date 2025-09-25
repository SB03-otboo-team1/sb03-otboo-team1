package com.onepiece.otboo.domain.profile.service;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.service.LocationPersistenceService;
import com.onepiece.otboo.domain.profile.dto.request.ProfileUpdateRequest;
import com.onepiece.otboo.domain.profile.dto.response.ProfileDto;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.exception.ProfileNotFoundException;
import com.onepiece.otboo.domain.profile.mapper.ProfileMapper;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.global.util.ArrayUtil;
import com.onepiece.otboo.global.util.NumberConverter;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final LocationPersistenceService locationPersistenceService;
    private final FileStorage storage;

    @Value("${aws.storage.prefix.profile}")
    private String PROFILE_PREFIX;

    @Override
    @Transactional(readOnly = true)
    public ProfileDto getUserProfile(UUID userId) {
        User user = findUser(userId);
        Profile profile = findProfile(userId);

        log.info("[ProfileService] 프로필 조회 완료 - userId: {}", userId);

        return profileMapper.toDto(user, profile, storage);
    }

    @Override
    @Transactional
    public ProfileDto update(UUID userId, ProfileUpdateRequest request,
        MultipartFile profileImage) throws IOException {
        User user = findUser(userId);
        Profile profile = findProfile(userId);

        if (request.location() != null) {
            Location location = findOrCreateLocation(request.location());
            profile.updateLocation(location);
        }

        profile.updateNickname(request.name());

        if (request.gender() != null) {
            profile.updateGender(request.gender());
        }

        if (request.birthDate() != null) {
            profile.updateBirthDate(request.birthDate());
        }

        if (request.temperatureSensitivity() != null) {
            profile.updateTempSensitivity(request.temperatureSensitivity());
        }

        updateProfileImage(profile, profileImage);

        Profile updatedProfile = profileRepository.save(profile);

        log.info("[ProfileService] 프로필 업데이트 성공 - userId: {}", userId);

        return profileMapper.toDto(user, updatedProfile, storage);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));
    }

    private Profile findProfile(UUID userId) {
        return profileRepository.findByUserId(userId)
            .orElseThrow(() -> new ProfileNotFoundException(userId));
    }

    private Location findOrCreateLocation(WeatherAPILocation dto) {
        double roundedLat = NumberConverter.round(dto.latitude(), 4);
        double roundedLon = NumberConverter.round(dto.longitude(), 4);

        return locationPersistenceService
            .findByLatitudeAndLongitude(roundedLat, roundedLon)
            .orElseGet(() -> {
                Integer x = dto.x();
                Integer y = dto.y();
                String names = ArrayUtil.joinString(dto.locationNames());
                Location location = Location.builder()
                    .latitude(roundedLat)
                    .longitude(roundedLon)
                    .xCoordinate(x)
                    .yCoordinate(y)
                    .locationNames(names)
                    .build();

                return locationPersistenceService.save(location);
            });
    }

    private void updateProfileImage(Profile profile, MultipartFile profileImage)
        throws IOException {
        if (profileImage == null) {
            return;
        }
        String currentKey = profile.getProfileImageUrl();

        if (profileImage.isEmpty()) {
            if (currentKey != null) {
                storage.deleteFile(currentKey);
                profile.updateProfileImageUrl(null);
            }
            return;
        }

        String newKey = storage.uploadFile(PROFILE_PREFIX, profileImage);
        profile.updateProfileImageUrl(newKey);

        if (currentKey != null && !currentKey.equals(newKey)) {
            storage.deleteFile(currentKey);
        }
    }
}
