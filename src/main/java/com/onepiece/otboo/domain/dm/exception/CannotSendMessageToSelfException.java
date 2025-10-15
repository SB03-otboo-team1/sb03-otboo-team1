package com.onepiece.otboo.domain.dm.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;

public class CannotSendMessageToSelfException extends GlobalException {

    public CannotSendMessageToSelfException() {
        super(ErrorCode.CANNOT_SEND_MESSAGE_TO_SELF);
    }
}