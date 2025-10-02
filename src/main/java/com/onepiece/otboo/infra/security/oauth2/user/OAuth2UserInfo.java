package com.onepiece.otboo.infra.security.oauth2.user;

import com.onepiece.otboo.domain.user.enums.Provider;
import java.util.Map;

public interface OAuth2UserInfo {

    Provider getProvider();

    String getProviderUserId();

    String getEmail();

    String getNickname();

    String getProfileImageUrl();

    Map<String, Object> getRawAttributes();
}
