package com.onepiece.otboo.domain.profile.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ProfileNotFoundException extends ProfileException {

    public ProfileNotFoundException(UUID userId) {
        super(ErrorCode.PROFILE_NOT_FOUND, Map.of("userId", userId));
    }
}
