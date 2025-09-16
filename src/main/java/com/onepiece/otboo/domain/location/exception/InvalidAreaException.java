package com.onepiece.otboo.domain.location.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;

public class InvalidAreaException extends LocationException {

    public InvalidAreaException(double latitude, double longitude) {
        super(ErrorCode.INVALID_AREA,
            Map.of("위도", latitude, "경도", longitude));
    }
}
