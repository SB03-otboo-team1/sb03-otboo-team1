package com.onepiece.otboo.domain.auth.service;

import com.onepiece.otboo.domain.auth.exception.UnsupportedProviderException;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.repository.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CustomUserDetailsMapper customUserDetailsMapper;

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
        if (existingUser != null) {
            updateExistingUserAndProfile(existingUser, provider, providerUserId, nickname,
                profileImageUrl);
        } else {
            createNewUserAndProfile(provider, providerUserId, email, nickname,
                profileImageUrl);
        }

        User savedUser = userRepository.findByEmail(email).orElse(null);
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

    /**
     * 기존 유저 및 프로필 정보 갱신
     */
    private void updateExistingUserAndProfile(User user, Provider provider, String providerUserId,
        String nickname, String profileImageUrl) {
        // 계정이 잠겨 있으면 로그인 거부
        if (user.isLocked()) {
            throw new OAuth2AuthenticationException("접근 거부되었습니다.");
        }
        // 소셜 계정이 연결되어 있지 않거나 정보가 다르면 연결
        var socialAccount = user.getSocialAccount();
        if (socialAccount == null || !socialAccount.isValid() || !socialAccount.isSameProviderAndId(
            provider, providerUserId)) {
            user.linkSocialAccount(provider, providerUserId);
            userRepository.save(user);
        }
        // 프로필이 없으면 생성, 기본 정보가 없으면 채움
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            profile = Profile.builder()
                .user(user)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();
            profileRepository.save(profile);
        } else {
            if ((profile.getNickname() == null || profile.getNickname().isBlank())
                && nickname != null) {
                profile.updateNickname(nickname);
            }
            if ((profile.getProfileImageUrl() == null || profile.getProfileImageUrl().isBlank())
                && profileImageUrl != null) {
                profile.updateProfileImageUrl(profileImageUrl);
            }
        }
    }

    /**
     * 신규 유저 및 프로필 생성
     */
    private void createNewUserAndProfile(Provider provider, String providerUserId, String email,
        String nickname, String profileImageUrl) {
        User newUser = User.builder()
            .socialAccount(SocialAccount.builder()
                .provider(provider)
                .providerUserId(providerUserId)
                .build())
            .email(email)
            .password("")
            .role(Role.USER)
            .locked(false)
            .build();
        newUser = userRepository.save(newUser);

        Profile profile = Profile.builder()
            .user(newUser)
            .nickname(nickname)
            .profileImageUrl(profileImageUrl)
            .build();
        profileRepository.save(profile);
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
