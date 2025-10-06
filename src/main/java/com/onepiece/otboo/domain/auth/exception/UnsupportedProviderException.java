package com.onepiece.otboo.domain.auth.exception;

import com.onepiece.otboo.global.exception.ErrorCode;

public class UnsupportedProviderException extends CustomAuthException {

    public UnsupportedProviderException() {
        super(ErrorCode.UNSUPPORTED_PROVIDER);
    }
}