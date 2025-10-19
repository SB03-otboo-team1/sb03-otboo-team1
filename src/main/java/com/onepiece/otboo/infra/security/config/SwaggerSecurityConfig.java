package com.onepiece.otboo.infra.security.config;

import com.onepiece.otboo.infra.security.config.factory.SecurityFilterChainFactory;
import com.onepiece.otboo.infra.security.config.helper.SecurityAuthorizeRequestHelper;
import com.onepiece.otboo.infra.security.config.props.SecurityProperties;
import com.onepiece.otboo.infra.security.config.props.SwaggerBasicAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class SwaggerSecurityConfig {

    private final SecurityProperties securityProperties;
    private final SecurityFilterChainFactory securityFilterChainFactory;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Order(SecurityChainOrder.SWAGGER)
    SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        SwaggerBasicAuthProperties basic = securityProperties.swagger().basic();
        http.securityMatcher(SecurityAuthorizeRequestHelper.swaggerPatterns());

        if (basic.enabled()) {
            basic.validateEnabledState();
            http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                    session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

            http.userDetailsService(swaggerUserDetailsService(basic));
            return http.build();
        }

        return securityFilterChainFactory.build(
            http,
            auth -> SecurityAuthorizeRequestHelper.permitEndpoints(
                auth,
                SecurityAuthorizeRequestHelper.swaggerEndpoints()
            )
        );
    }

    private UserDetailsService swaggerUserDetailsService(SwaggerBasicAuthProperties basic) {
        return new InMemoryUserDetailsManager(
            User.builder()
                .username(basic.username())
                .password(passwordEncoder.encode(basic.password()))
                .roles("SWAGGER")
                .build()
        );
    }
}


