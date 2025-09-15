package com.onepiece.otboo.domain.user.entity;

import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.global.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseUpdatableEntity {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(name = "provider_user_id")
    private String providerUserId;

    @Column(nullable = false)
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

    public void updatePassword(String password) {
        this.password = password;
        this.temporaryPassword = null;
        this.temporaryPasswordExpirationTime = null;
    }

    public void updateTemporaryPassword(String tempPassword, Instant expirationTime) {
        this.temporaryPassword = tempPassword;
        this.temporaryPasswordExpirationTime = expirationTime;
    }

    public boolean isTemporaryPasswordValid(String inputPassword) {
        if (this.temporaryPassword == null || this.temporaryPasswordExpirationTime == null) {
            return false;
        }
        return this.temporaryPassword.equals(inputPassword)
            && Instant.now().isBefore(this.temporaryPasswordExpirationTime);
    }

    public void updateLocked(boolean locked) {
        this.locked = locked;
    }

    public void updateRole(Role role) {
        this.role = role;
    }
}
