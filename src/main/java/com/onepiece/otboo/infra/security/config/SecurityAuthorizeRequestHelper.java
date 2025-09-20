package com.onepiece.otboo.infra.security.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

final class SecurityAuthorizeRequestHelper {

    private static final String[] PUBLIC_RESOURCES = {
        "/",
        "/index.html",
        "logo_symbol.svg",
        "vite.svg",
        "/error",
        "/favicon.ico",
        "/assets/**",
        "/static/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/api/users",
        "/api/weathers/**"
    };

    private SecurityAuthorizeRequestHelper() {
    }

    static void permitCommonPublicEndpoints(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        registry.requestMatchers(PUBLIC_RESOURCES).permitAll();
        registry.requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll();
        registry.requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll();
        registry.requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll();
        registry.requestMatchers(HttpMethod.POST, "/api/auth/sign-in").permitAll();
    }
}
