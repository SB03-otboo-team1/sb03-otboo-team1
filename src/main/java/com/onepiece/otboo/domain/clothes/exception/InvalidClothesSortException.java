package com.onepiece.otboo.domain.clothes.exception;

import com.onepiece.otboo.global.exception.ErrorCode;

public class InvalidClothesSortException extends ClothesException {

    public InvalidClothesSortException() {
        super(ErrorCode.INVALID_CLOTHES_SORT);
    }
}
