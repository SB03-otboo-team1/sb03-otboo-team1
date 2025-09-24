package com.onepiece.otboo.domain.user.entity;

import com.onepiece.otboo.domain.user.enums.Provider;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 계정 연동 정보를 위한 임베디드 타입
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SocialAccount {

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider = Provider.LOCAL;

    @Column(name = "provider_user_id")
    private String providerUserId;

    public boolean isSameProviderAndId(Provider provider, String providerUserId) {
        return this.provider == provider && Objects.equals(this.providerUserId,
            providerUserId);
    }

    public boolean isValid() {
        return provider != null && providerUserId != null && !providerUserId.isBlank();
    }
}
