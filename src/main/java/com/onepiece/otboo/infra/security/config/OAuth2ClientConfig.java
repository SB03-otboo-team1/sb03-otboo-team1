package com.onepiece.otboo.infra.security.config;

import com.onepiece.otboo.domain.auth.service.CustomOAuth2UserService;
import com.onepiece.otboo.infra.security.oauth2.handler.CustomOAuth2FailureHandler;
import com.onepiece.otboo.infra.security.oauth2.handler.CustomOAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class OAuth2ClientConfig {

    @Bean
    @Order(SecurityChainOrder.OAUTH2_CLIENT)
    public SecurityFilterChain oAuth2ClientSecurityFilterChain(
        HttpSecurity http,
        CustomOAuth2UserService customOAuth2UserService,
        CustomOAuth2SuccessHandler delegatingSuccessHandler,
        CustomOAuth2FailureHandler delegatingFailureHandler
    ) throws Exception {
        http
            .securityMatcher("/oauth2/authorization/**", "/login/oauth2/code/**")
            .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(delegatingSuccessHandler)
                .failureHandler(delegatingFailureHandler)
            );
        return http.build();
    }
}