package com.onepiece.otboo.infra.security.oauth2.user;

import com.onepiece.otboo.domain.auth.exception.UnsupportedProviderException;
import com.onepiece.otboo.domain.user.enums.Provider;
import java.util.Map;

public final class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static OAuth2UserInfo from(Provider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);
            default -> throw new UnsupportedProviderException();
        };
    }
}
