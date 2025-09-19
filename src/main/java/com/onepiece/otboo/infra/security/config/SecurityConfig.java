package com.onepiece.otboo.infra.security.config;

import com.onepiece.otboo.infra.security.auth.CustomAuthenticationProvider;
import com.onepiece.otboo.infra.security.handler.JwtLoginFailureHandler;
import com.onepiece.otboo.infra.security.handler.JwtLoginSuccessHandler;
import com.onepiece.otboo.infra.security.handler.JwtLogoutHandler;
import com.onepiece.otboo.infra.security.handler.RestAccessDeniedHandler;
import com.onepiece.otboo.infra.security.handler.RestAuthenticationEntryPoint;
import com.onepiece.otboo.infra.security.handler.SpaCsrfTokenRequestHandler;
import com.onepiece.otboo.infra.security.jwt.JwtAuthenticationFilter;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtProvider jwtTokenProvider,
        JwtLoginSuccessHandler jwtLoginSuccessHandler,
        JwtLoginFailureHandler jwtLoginFailureHandler,
        JwtLogoutHandler jwtLogoutHandler,
        CustomAuthenticationProvider customAuthenticationProvider,
        SecurityProperties securityProperties,
        RestAuthenticationEntryPoint restAuthenticationEntryPoint,
        RestAccessDeniedHandler restAccessDeniedHandler
    ) throws Exception {

        AuthenticationManager authenticationManager = http.getSharedObject(
                AuthenticationManagerBuilder.class)
            .authenticationProvider(customAuthenticationProvider)
            .build();

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider);
        UsernamePasswordAuthenticationFilter loginFilter = usernamePasswordAuthenticationFilter(
            authenticationManager,
            jwtLoginSuccessHandler,
            jwtLoginFailureHandler
        );

        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(securityProperties.csrf().ignoredRequestMatchers())
                .csrfTokenRepository(csrfRepo())
                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
            )
            .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restAccessDeniedHandler)
            )
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**",
                    "/",
                    "/index.html", "logo_symbol.svg", "vite.svg",
                    "/error",
                    "/favicon.ico",
                    "/assets/**",
                    "/static/**",
                    "/swagger-ui/**", "/v3/api-docs/**",
                    "/api/users", "/api/weathers/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/sign-in").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
            .authenticationManager(authenticationManager)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(logout -> logout
                .logoutUrl("/api/auth/sign-out")
                .addLogoutHandler(jwtLogoutHandler)
                .logoutSuccessHandler(
                    new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
            );

        return http.build();
    }

    private UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter(
        AuthenticationManager authenticationManager,
        JwtLoginSuccessHandler successHandler,
        JwtLoginFailureHandler failureHandler
    ) {
        UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter();
        filter.setFilterProcessesUrl("/api/auth/sign-in");
        filter.setUsernameParameter("email");
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setAuthenticationFailureHandler(failureHandler);
        filter.setAllowSessionCreation(false);
        return filter;
    }

    @Bean
    public CookieCsrfTokenRepository csrfRepo() {
        CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repo.setCookieName("XSRF-TOKEN");
        repo.setHeaderName("X-XSRF-TOKEN");
        return repo;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}