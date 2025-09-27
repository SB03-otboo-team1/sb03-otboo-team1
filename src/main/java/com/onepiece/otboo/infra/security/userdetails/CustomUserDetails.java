package com.onepiece.otboo.infra.security.userdetails;

import com.onepiece.otboo.domain.auth.exception.UnAuthorizedException;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.enums.Role;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Slf4j
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final UUID userId;
    private final String email;
    private final String password;
    private final Role role;
    private final boolean locked;
    private final String temporaryPassword;
    private final Instant temporaryPasswordExpirationTime;
    private Map<String, Object> attributes;

    private UserDto userDto;

    public void updateUserDto(UserDto userDto) {
        this.userDto = userDto;
    }

    public void updateAttributes(Map<String, Object> newAttributes) {
        if (newAttributes == null) {
            throw new UnAuthorizedException();
        }

        Map<String, Object> sanitized = newAttributes.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue() == null ? "" : e.getValue()
            ));

        String[] requiredKeys = {"provider", "providerUserId", "email", "nickname"};
        for (String key : requiredKeys) {
            String value = (String) sanitized.get(key);
            if (value == null || value.isBlank()) {
                log.debug("OAuth2 시도 중 필수 attributes 누락: {}", key);
                throw new UnAuthorizedException();
            }
        }
        this.attributes = sanitized;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * UserDetails의 username "email"
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * OAuth2User의 attributes "name"
     */
    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}
