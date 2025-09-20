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
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "prod", "test-security"})
@RequiredArgsConstructor
public class SecurityFilterChainFactory {

    private final JwtProvider jwtProvider;
    private final JwtLoginSuccessHandler jwtLoginSuccessHandler;
    private final JwtLoginFailureHandler jwtLoginFailureHandler;
    private final JwtLogoutHandler jwtLogoutHandler;
    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final SecurityProperties securityProperties;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final CookieCsrfTokenRepository cookieCsrfTokenRepository;

    public SecurityFilterChain build(
        HttpSecurity http,
        Consumer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> authorizeCustomizer
    ) throws Exception {
        AuthenticationManager authenticationManager = http.getSharedObject(
                AuthenticationManagerBuilder.class)
            .authenticationProvider(customAuthenticationProvider)
            .build();

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtProvider);
        UsernamePasswordAuthenticationFilter loginFilter = usernamePasswordAuthenticationFilter(
            authenticationManager
        );

        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(securityProperties.csrf().ignoredRequestMatchers())
                .csrfTokenRepository(cookieCsrfTokenRepository)
                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
            )
            .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restAccessDeniedHandler)
            )
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                authorizeCustomizer.accept(auth);
                auth.anyRequest().authenticated();
            })
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
        AuthenticationManager authenticationManager
    ) {
        UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter();
        filter.setFilterProcessesUrl("/api/auth/sign-in");
        filter.setUsernameParameter("email");
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(jwtLoginSuccessHandler);
        filter.setAuthenticationFailureHandler(jwtLoginFailureHandler);
        filter.setAllowSessionCreation(false);
        return filter;
    }
}
