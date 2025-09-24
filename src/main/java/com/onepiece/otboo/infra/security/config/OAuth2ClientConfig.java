package com.onepiece.otboo.infra.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class OAuth2ClientConfig {

    @Bean
    @Order(SecurityChainOrder.OAUTH2_CLIENT)
    public SecurityFilterChain oAuth2ClientSecurityFilterChain(HttpSecurity http,
        ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
            .securityMatcher("/oauth2/**", "/login/oauth2/**")
            .oauth2Login(oauth2 -> oauth2
                .clientRegistrationRepository(clientRegistrationRepository)
            );
        // TODO: 성공/실패 핸들러 등 커스터마이징
        return http.build();
    }
}