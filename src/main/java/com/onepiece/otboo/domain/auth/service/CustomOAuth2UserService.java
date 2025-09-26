package com.onepiece.otboo.domain.auth.service;

import com.onepiece.otboo.domain.auth.exception.UnsupportedProviderException;
import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소셜 인증 연동/매핑 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        Provider provider = Provider.valueOf(registrationId.toUpperCase());
        String providerUserId;
        String email;
        switch (provider) {
            case GOOGLE:
                providerUserId = getGoogleProviderUserId(oAuth2User);
                email = getGoogleEmail(oAuth2User);
                break;
            case KAKAO:
                providerUserId = getKakaoProviderUserId(oAuth2User);
                email = getKakaoEmail(oAuth2User);
                break;
            default:
                throw new UnsupportedProviderException();
        }

        User user = linkOrCreateUser(provider, providerUserId, email);

        // 인증 컨텍스트를 CustomUserDetails로 래핑
        Map<String, Object> attributes = oAuth2User.getAttributes();
        attributes.put("userId", user.getId());
        attributes.put("email", user.getEmail());
        attributes.put("role", user.getRole());
        attributes.put("provider", provider.name());
        attributes.put("providerUserId", providerUserId);

        CustomUserDetails customUserDetails = new CustomUserDetails(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user.getRole(),
            user.isLocked(),
            user.getTemporaryPassword(),
            user.getTemporaryPasswordExpirationTime()
        );
        customUserDetails.setAttributes(attributes);
        return customUserDetails;
    }

    private String getGoogleProviderUserId(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("sub");
    }

    private String getGoogleEmail(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("email");
    }

    private String getKakaoProviderUserId(OAuth2User oAuth2User) {
        Object idObj = oAuth2User.getAttribute("id");
        return idObj != null ? String.valueOf(idObj) : "";
    }

    private String getKakaoEmail(OAuth2User oAuth2User) {
        Object propertiesObj = oAuth2User.getAttribute("properties");
        if (propertiesObj instanceof Map) {
            Object nickObj = ((Map<?, ?>) propertiesObj).get("nickname");
            return (nickObj != null ? nickObj.toString() : "unknown") + "@kakao.com";
        }
        return "unknown@kakao.com";
    }

    public User linkOrCreateUser(Provider provider, String providerUserId, String email) {
        SocialAccount socialAccount = SocialAccount.builder()
            .provider(provider)
            .providerUserId(providerUserId)
            .build();

        Optional<User> byProvider = userRepository.findBySocialAccountProviderAndSocialAccountProviderUserId(
            provider, providerUserId);
        if (byProvider.isPresent()) {
            return byProvider.get();
        }

        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            User updated = User.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .temporaryPassword(user.getTemporaryPassword())
                .temporaryPasswordExpirationTime(user.getTemporaryPasswordExpirationTime())
                .locked(user.isLocked())
                .role(user.getRole())
                .socialAccount(socialAccount)
                .build();
            return userRepository.save(updated);
        }

        User newUser = User.builder()
            .email(email)
            .socialAccount(socialAccount)
            .password("")
            .locked(false)
            .role(Role.USER)
            .build();

        return userRepository.save(newUser);
    }
}
