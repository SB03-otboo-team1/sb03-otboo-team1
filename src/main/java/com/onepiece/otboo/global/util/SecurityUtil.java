package com.onepiece.otboo.global.util;

import com.onepiece.otboo.domain.auth.exception.UnAuthorizedException;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    // 사용자 ID 와 인증된 사용자 ID 일치하는지 점검하는 코드
    public static boolean isRequester(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new UnAuthorizedException();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return userId.equals(cud.getUserId());
        }
        try {
            return userId.equals(UUID.fromString(authentication.getName()));
        } catch (UnAuthorizedException e) {
            throw new UnAuthorizedException();
        }
    }

    public static void requireAuthorizedUser(UUID userId) {
        if (!isRequester(userId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }
    }
}
