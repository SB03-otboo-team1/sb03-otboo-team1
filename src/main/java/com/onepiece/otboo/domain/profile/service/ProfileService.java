package com.onepiece.otboo.domain.profile.service;

import com.onepiece.otboo.domain.profile.dto.request.ProfileUpdateRequest;
import com.onepiece.otboo.domain.profile.dto.response.ProfileDto;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileDto getUserProfile(UUID userId);

    ProfileDto update(UUID userId, ProfileUpdateRequest request, MultipartFile profileImage);
}
