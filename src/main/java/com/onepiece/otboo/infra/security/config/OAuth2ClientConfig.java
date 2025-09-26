package com.onepiece.otboo.infra.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class OAuth2ClientConfig {

    @Bean
    @Order(SecurityChainOrder.OAUTH2_CLIENT)
    public SecurityFilterChain oAuth2ClientSecurityFilterChain(
        HttpSecurity http,
        OAuth2UserService mockOAuth2UserService,
        AuthenticationSuccessHandler mockSuccessHandler,
        AuthenticationFailureHandler mockFailureHandler
    ) throws Exception {
        http
            .securityMatcher("/oauth2/**", "/login/oauth2/**")
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(mockOAuth2UserService)
                )
                .successHandler(mockSuccessHandler)
                .failureHandler(mockFailureHandler)
            );
        return http.build();
    }

    @Bean
    public OAuth2UserService mockOAuth2UserService() {
        return (request) -> null; // TODO: 구현 예정
    }

    @Bean
    public AuthenticationSuccessHandler mockSuccessHandler() {
        return (request, response, authentication) -> {
            // TODO: 구현 예정
            response.setStatus(200);
        };
    }

    @Bean
    public AuthenticationFailureHandler mockFailureHandler() {
        return (request, response, exception) -> {
            // TODO: 구현 예정
            response.setStatus(401);
        };
    }
}