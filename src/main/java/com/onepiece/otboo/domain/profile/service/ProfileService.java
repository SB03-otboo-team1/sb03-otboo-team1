package com.onepiece.otboo.domain.profile.service;

import com.onepiece.otboo.domain.profile.dto.response.ProfileDto;
import java.util.UUID;

public interface ProfileService {

    ProfileDto getUserProfile(UUID userId);
}
