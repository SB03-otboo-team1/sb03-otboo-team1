package com.onepiece.otboo.domain.user.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class UserNotFoundException extends UserException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }

    private UserNotFoundException(Map<String, Object> details) {
        super(ErrorCode.USER_NOT_FOUND, details);
    }

    public static UserNotFoundException byId(UUID userId) {
        return new UserNotFoundException(Map.of("userId", userId));
    }

    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException(Map.of("email", email));
    }

    public static UserNotFoundException byName(String name) {
        return new UserNotFoundException(Map.of("userName", name));
    }
}
