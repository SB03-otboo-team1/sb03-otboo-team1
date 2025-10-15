package com.onepiece.otboo.infra.security.config.factory;

import com.onepiece.otboo.infra.security.auth.CustomAuthenticationProvider;
import com.onepiece.otboo.infra.security.config.props.SecurityProperties;
import com.onepiece.otboo.infra.security.handler.RestAccessDeniedHandler;
import com.onepiece.otboo.infra.security.handler.RestAuthenticationEntryPoint;
import com.onepiece.otboo.infra.security.handler.SpaCsrfTokenRequestHandler;
import com.onepiece.otboo.infra.security.jwt.JwtAuthenticationFilter;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import com.onepiece.otboo.infra.security.jwt.handler.JwtLoginFailureHandler;
import com.onepiece.otboo.infra.security.jwt.handler.JwtLoginSuccessHandler;
import com.onepiece.otboo.infra.security.jwt.handler.JwtLogoutHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
import org.springframework.web.cors.CorsConfiguration;

@Component
@Profile("!test")
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
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(securityProperties.cors().allowedOrigins());
                config.setAllowedMethods(securityProperties.cors().allowedMethods());
                config.setAllowedHeaders(securityProperties.cors().allowedHeaders());
                config.setAllowCredentials(securityProperties.cors().allowCredentials());
                return config;
            }))
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
        UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter() {
            @Override
            protected String obtainPassword(HttpServletRequest request) {
                String password = super.obtainPassword(request);
                if (password == null || password.isBlank()) {
                    throw new BadCredentialsException("비밀번호를 입력해 주세요.");
                }
                return password;
            }
        };
        filter.setFilterProcessesUrl("/api/auth/sign-in");
        filter.setUsernameParameter("username");
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(jwtLoginSuccessHandler);
        filter.setAuthenticationFailureHandler(jwtLoginFailureHandler);
        filter.setAllowSessionCreation(false);
        return filter;
    }
}
