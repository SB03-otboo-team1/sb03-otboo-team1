package com.onepiece.otboo.infra.security.config.props;

import org.springframework.util.StringUtils;

public record SwaggerBasicAuthProperties(
    boolean enabled,
    String username,
    String password
) {

    public static SwaggerBasicAuthProperties disabled() {
        return new SwaggerBasicAuthProperties(false, null, null);
    }

    public void validateEnabledState() {
        if (!enabled) {
            return;
        }

        if (!StringUtils.hasText(username)) {
            throw new IllegalStateException("Swagger 기본 인증 누락: username");
        }

        if (!StringUtils.hasText(password)) {
            throw new IllegalStateException("Swagger 기본 인증 누락: password");
        }
    }
}
