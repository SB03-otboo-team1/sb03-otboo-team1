package com.onepiece.otboo.domain.follow.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class DuplicateFollowException extends FollowException {

    public DuplicateFollowException() {
        super(ErrorCode.DUPLICATE_FOLLOW);
    }

    private DuplicateFollowException(Map<String, Object> details) {
        super(ErrorCode.DUPLICATE_FOLLOW, details);
    }

    public static DuplicateFollowException of(UUID followerId, UUID followingId) {
        return new DuplicateFollowException(Map.of(
            "followerId", followerId,
            "followingId", followingId
        ));
    }
}
