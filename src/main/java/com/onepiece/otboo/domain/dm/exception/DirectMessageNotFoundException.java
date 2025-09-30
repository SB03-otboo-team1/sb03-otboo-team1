package com.onepiece.otboo.domain.dm.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;

public class DirectMessageNotFoundException extends GlobalException {

    public DirectMessageNotFoundException() {
        super(ErrorCode.DM_NOT_FOUND);
    }
}