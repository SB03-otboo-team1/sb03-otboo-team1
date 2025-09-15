package com.onepiece.otboo.infra.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // H2 콘솔 & 회원 API는 CSRF 검사 제외 (개발용)
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/users/**"))

            .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**",
                    "/",
                    "/index.html", "logo_symbol.svg", "vite.svg",
                    "/error",
                    "/favicon.ico",
                    "/assets/**",
                    "/static/**",
                    "/swagger-ui/**", "/v3/api-docs/**",
                    "/api/users").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
