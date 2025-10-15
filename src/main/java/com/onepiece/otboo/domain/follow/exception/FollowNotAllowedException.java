package com.onepiece.otboo.domain.follow.exception;

import com.onepiece.otboo.global.exception.ErrorCode;

public class FollowNotAllowedException extends FollowException {

    public FollowNotAllowedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
