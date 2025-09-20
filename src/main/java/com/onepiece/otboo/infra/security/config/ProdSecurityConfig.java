package com.onepiece.otboo.infra.security.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("prod")
@RequiredArgsConstructor
public class ProdSecurityConfig {

    private final SecurityFilterChainFactory securityFilterChainFactory;

    @Bean
    @Order(SecurityChainOrder.APPLICATION)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return securityFilterChainFactory.build(
            http,
            auth -> SecurityAuthorizeRequestHelper.permitEndpoints(
                auth,
                SecurityAuthorizeRequestHelper.commonPublicEndpoints()
            )
        );
    }

    @PostConstruct
    void init() {
        log.info("[ProdSecurityConfig] activated");
    }
}
