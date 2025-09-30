package com.onepiece.otboo.infra.security.oauth2.user;

import com.onepiece.otboo.domain.user.enums.Provider;
import java.util.Map;

public record KakaoOAuth2UserInfo(Map<String, Object> attributes) implements OAuth2UserInfo {

    @Override
    public Provider getProvider() {
        return Provider.KAKAO;
    }

    @Override
    public String getProviderUserId() {
        Object id = attributes.get("id");
        return id != null ? id.toString() : null;
    }

    @Override
    public String getEmail() {
        Object account = attributes.get("kakao_account");
        if (account instanceof Map<?, ?> acc) {
            Object e = acc.get("email");
            if (e != null) {
                return e.toString();
            }
        }
        String providerUserId = getProviderUserId();
        if (providerUserId != null) {
            return "kakao_" + providerUserId + "@kakao.com";
        }
        // providerUserId도 없으면 닉네임 기반(이론상 거의 없음)
        return getNickname() + "@kakao.com";
    }

    @Override
    public String getNickname() {
        Object properties = attributes.get("properties");
        // 닉네임은 필수 동의이므로 반드시 존재
        return ((Map<?, ?>) properties).get("nickname").toString();
    }

    @Override
    public String getProfileImageUrl() {
        Object properties = attributes.get("properties");
        if (properties instanceof Map<?, ?> map) {
            Object img = map.get("profile_image");
            if (img == null) {
                img = map.get("profile_image_url");
            }
            return img != null ? img.toString() : null;
        }
        return null;
    }

    @Override
    public Map<String, Object> getRawAttributes() {
        return attributes;
    }
}
