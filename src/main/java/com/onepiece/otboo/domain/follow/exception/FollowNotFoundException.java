package com.onepiece.otboo.domain.follow.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class FollowNotFoundException extends FollowException {

    public FollowNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    private FollowNotFoundException(Map<String, Object> details) {
        super(ErrorCode.FOLLOW_NOT_FOUND, details);
    }

    public static FollowNotFoundException of(UUID followerId, UUID followeeId) {
        return new FollowNotFoundException(Map.of(
            "followerId", followerId,
            "followeeId", followeeId
        ));
    }
}