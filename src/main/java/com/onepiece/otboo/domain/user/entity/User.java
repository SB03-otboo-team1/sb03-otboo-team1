package com.onepiece.otboo.domain.user.entity;

import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.global.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseUpdatableEntity {

    @Embedded
    private SocialAccount socialAccount;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "temporary_password")
    private String temporaryPassword;

    @Column(name = "temporary_password_expiration_time")
    private Instant temporaryPasswordExpirationTime;

    @Column(nullable = false)
    private boolean locked;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Profile profile;

    public void updatePassword(String password) {
        this.password = password;
        this.temporaryPassword = null;
        this.temporaryPasswordExpirationTime = null;
    }

    public void updateTemporaryPassword(String rawPassword, PasswordEncoder encoder,
        long validitySeconds) {
        this.temporaryPassword = encoder.encode(rawPassword);
        this.temporaryPasswordExpirationTime = Instant.now().plusSeconds(validitySeconds);
    }

    public void clearTemporaryPassword() {
        this.temporaryPassword = null;
        this.temporaryPasswordExpirationTime = null;
    }

    public boolean isTemporaryPasswordValid(String inputPassword, PasswordEncoder encoder) {
        if (this.temporaryPassword == null || this.temporaryPasswordExpirationTime == null) {
            return false;
        }
        return encoder.matches(inputPassword, this.temporaryPassword)
            && Instant.now().isBefore(this.temporaryPasswordExpirationTime);
    }

    public void updateLocked(boolean locked) {
        this.locked = locked;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    /**
     * 소셜 계정 연동
     * <p>
     * 현재 서비스의 구조는 소셜 로그인 또는 가입 시 고유한 이메일을 조회하여 신규 생성 또는 연동 업데이트 방식.
     * <p>
     * 하나의 계정에 여러 소셜 계정을 연결하는 것을 지원하지 않음.
     */
    public void linkSocialAccount(Provider provider, String providerUserId) {
        if (this.socialAccount != null && this.socialAccount.isValid()) {
            if (!this.socialAccount.isSameProviderAndId(provider, providerUserId)) {
                throw new IllegalStateException("이미 다른 소셜 계정이 연동되어 있습니다.");
            }
            return;
        }
        this.socialAccount = SocialAccount.builder()
            .provider(provider)
            .providerUserId(providerUserId)
            .build();
        if (!this.socialAccount.isValid()) {
            throw new IllegalArgumentException("유효하지 않은 소셜 계정 정보입니다.");
        }
    }

    public String getNickname() {
        return (profile != null) ? profile.getNickname() : null;
    }

    public String getProfileImage() {
        return (profile != null) ? profile.getProfileImageUrl() : null;
    }

}
