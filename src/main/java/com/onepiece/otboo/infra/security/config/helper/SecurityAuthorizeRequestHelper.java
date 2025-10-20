package com.onepiece.otboo.infra.security.config.helper;

import com.onepiece.otboo.infra.security.dto.data.Endpoint;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

public final class SecurityAuthorizeRequestHelper {

    private static final Endpoint[] COMMON_PUBLIC_ENDPOINTS = {
        new Endpoint(null, "/"),
        new Endpoint(null, "/index.html"),
        new Endpoint(null, "/logo_symbol.svg"),
        new Endpoint(null, "/vite.svg"),
        new Endpoint(null, "/error"),
        new Endpoint(null, "/favicon.ico"),
        new Endpoint(null, "/assets/**"),
        new Endpoint(null, "/static/**"),
        new Endpoint(HttpMethod.GET, "/api/auth/csrf-token"),
        new Endpoint(HttpMethod.POST, "/api/auth/refresh"),
        new Endpoint(HttpMethod.POST, "/api/auth/reset-password"),
        new Endpoint(HttpMethod.POST, "/api/auth/sign-in"),
        new Endpoint(HttpMethod.POST, "/api/users"),
        new Endpoint(HttpMethod.GET, "/clothes/**"),
        new Endpoint(HttpMethod.GET, "/ws/**")
    };

    private static final Endpoint[] SWAGGER_ENDPOINTS = {
        new Endpoint(null, "/api/swagger-ui/**"),
        new Endpoint(null, "/api/v3/api-docs"),
        new Endpoint(null, "/api/v3/api-docs/**")
    };

    public static Endpoint[] commonPublicEndpoints() {
        return COMMON_PUBLIC_ENDPOINTS.clone();
    }

    public static Endpoint[] swaggerEndpoints() {
        return SWAGGER_ENDPOINTS.clone();
    }

    public static String[] swaggerPatterns() {
        String[] patterns = new String[SWAGGER_ENDPOINTS.length];
        for (int i = 0; i < SWAGGER_ENDPOINTS.length; i++) {
            patterns[i] = SWAGGER_ENDPOINTS[i].pattern();
        }
        return patterns;
    }

    public static void permitEndpoints(
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
