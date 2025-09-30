package com.onepiece.otboo.domain.auth.service;

import com.onepiece.otboo.domain.auth.exception.UnsupportedProviderException;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.domain.user.service.UserService;
import com.onepiece.otboo.infra.security.mapper.CustomUserDetailsMapper;
import com.onepiece.otboo.infra.security.oauth2.user.OAuth2UserInfo;
import com.onepiece.otboo.infra.security.oauth2.user.OAuth2UserInfoFactory;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final CustomUserDetailsMapper customUserDetailsMapper;
    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Provider provider = toProvider(registrationId);

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.from(provider, oAuth2User.getAttributes());
        String providerUserId = userInfo.getProviderUserId();
        String email = userInfo.getEmail();
        String nickname = userInfo.getNickname();
        String profileImageUrl = userInfo.getProfileImageUrl();

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("잘못된 요청입니다.");
        }

        User existingUser = userRepository.findByEmail(email).orElse(null);
        User savedUser;
        if (existingUser != null) {
            savedUser = userService.updateSocialAccountAndProfile(existingUser, provider,
                providerUserId,
                nickname,
                profileImageUrl);
        } else {
            savedUser = userService.createSocialUserAndProfile(provider, providerUserId, email,
                nickname,
                profileImageUrl);
        }
        if (savedUser == null) {
            throw new OAuth2AuthenticationException("사용자 정보를 찾을 수 없습니다.");
        }
        CustomUserDetails customUserDetails = customUserDetailsMapper.toCustomUserDetails(
            savedUser);
        customUserDetails.updateAttributes(Map.of(
            "provider", provider.name(),
            "providerUserId", providerUserId,
            "email", email,
            "nickname", nickname == null ? "" : nickname,
            "profileImageUrl", profileImageUrl == null ? "" : profileImageUrl
        ));
        return customUserDetails;
    }

    private Provider toProvider(String registrationId) {
        if (registrationId == null) {
            throw new UnsupportedProviderException();
        }
        return switch (registrationId.toLowerCase()) {
            case "google" -> Provider.GOOGLE;
            case "kakao" -> Provider.KAKAO;
            default -> throw new UnsupportedProviderException();
        };
    }
}
