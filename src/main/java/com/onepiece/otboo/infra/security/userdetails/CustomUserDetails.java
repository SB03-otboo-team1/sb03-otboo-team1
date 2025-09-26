package com.onepiece.otboo.infra.security.userdetails;

import com.onepiece.otboo.domain.user.enums.Role;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails, OAuth2User {

    @Override
    public String getName() {
        return email != null ? email : (userId != null ? userId.toString() : "");
    }

    private final UUID userId;
    private final String email;
    private final String password;
    private final Role role;
    private final boolean locked;
    private final String temporaryPassword;
    private final Instant temporaryPasswordExpirationTime;
    private Map<String, Object> attributes;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (temporaryPassword != null && temporaryPasswordExpirationTime != null) {
            if (Instant.now().isAfter(temporaryPasswordExpirationTime)) {
                log.info("로그인 실패: 임시 비밀번호가 만료되었습니다.");
                return false;
            }
        }
        return true;
    }

    public boolean isTemporaryPasswordValid(String inputPassword) {
        if (this.temporaryPassword == null || this.temporaryPasswordExpirationTime == null) {
            return false;
        }
        return this.temporaryPassword.equals(inputPassword)
            && Instant.now().isBefore(this.temporaryPasswordExpirationTime);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
