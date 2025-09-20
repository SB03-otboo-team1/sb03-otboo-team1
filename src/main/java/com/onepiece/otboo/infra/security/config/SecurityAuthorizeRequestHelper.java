package com.onepiece.otboo.infra.security.config;

import com.onepiece.otboo.infra.security.dto.data.Endpoint;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

final class SecurityAuthorizeRequestHelper {

    private static final Endpoint[] COMMON_PUBLIC_ENDPOINTS = {
        new Endpoint(null, "/"),
        new Endpoint(null, "/index.html"),
        new Endpoint(null, "logo_symbol.svg"),
        new Endpoint(null, "vite.svg"),
        new Endpoint(null, "/error"),
        new Endpoint(null, "/favicon.ico"),
        new Endpoint(null, "/assets/**"),
        new Endpoint(null, "/static/**"),
        new Endpoint(HttpMethod.GET, "/api/auth/csrf-token"),
        new Endpoint(HttpMethod.POST, "/api/auth/refresh"),
        new Endpoint(HttpMethod.POST, "/api/auth/reset-password"),
        new Endpoint(HttpMethod.POST, "/api/auth/sign-in")
    };

    private static final Endpoint[] SWAGGER_ENDPOINTS = {
        new Endpoint(null, "/swagger-ui/**"),
        new Endpoint(null, "/v3/api-docs/**")
    };

    static Endpoint[] commonPublicEndpoints() {
        return COMMON_PUBLIC_ENDPOINTS.clone();
    }

    static Endpoint[] swaggerEndpoints() {
        return SWAGGER_ENDPOINTS.clone();
    }

    static String[] swaggerPatterns() {
        String[] patterns = new String[SWAGGER_ENDPOINTS.length];
        for (int i = 0; i < SWAGGER_ENDPOINTS.length; i++) {
            patterns[i] = SWAGGER_ENDPOINTS[i].pattern();
        }
        return patterns;
    }

    static void permitEndpoints(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry,
        Endpoint... endpoints
    ) {
        for (Endpoint endpoint : endpoints) {
            if (endpoint.method() == null) {
                registry.requestMatchers(endpoint.pattern()).permitAll();
            } else {
                registry.requestMatchers(endpoint.method(), endpoint.pattern()).permitAll();
            }
        }
    }
}
