package com.onepiece.otboo.infra.security.oauth2.user;

import com.onepiece.otboo.domain.user.enums.Provider;
import java.util.Map;

public record GoogleOAuth2UserInfo(Map<String, Object> attributes) implements OAuth2UserInfo {

    @Override
    public Provider getProvider() {
        return Provider.GOOGLE;
    }

    @Override
    public String getProviderUserId() {
        Object sub = attributes.get("sub");
        return sub != null ? sub.toString() : null;
    }

    @Override
    public String getEmail() {
        Object email = attributes.get("email");
        return email != null ? email.toString() : null;
    }

    @Override
    public String getNickname() {
        Object name = attributes.get("name");
        return name != null ? name.toString() : null;
    }

    @Override
    public String getProfileImageUrl() {
        Object picture = attributes.get("picture");
        return picture != null ? picture.toString() : null;
    }

    @Override
    public Map<String, Object> getRawAttributes() {
        return attributes;
    }
}
