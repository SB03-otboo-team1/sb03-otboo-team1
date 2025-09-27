package com.onepiece.otboo.domain.auth.service;

import com.onepiece.otboo.domain.auth.exception.UnsupportedProviderException;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.mapper.UserMapper;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
    private final ProfileRepository profileRepository;
    private final UserMapper userMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Provider provider = toProvider(registrationId);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String providerUserId = extractProviderUserId(provider, attributes, userRequest);
        String email = extractEmail(provider, attributes);
        String nickname = extractNickname(provider, attributes);

        Optional<User> byProvider = userRepository
            .findBySocialAccountProviderAndSocialAccountProviderUserId(provider, providerUserId);
        Optional<User> byEmail =
            email == null ? Optional.empty() : userRepository.findByEmail(email);

        User user = byProvider.or(() -> byEmail).orElse(null);

        UUID userId = user == null ? null : user.getId();
        Role role = user == null ? Role.USER : user.getRole();
        boolean locked = user != null && user.isLocked();

        CustomUserDetails userDetails = new CustomUserDetails(
            userId,
            email,
            "",
            role,
            locked,
            null,
            null
        );

        userDetails.updateAttributes(Map.of(
            "provider", provider.name(),
            "providerUserId", providerUserId,
            "email", email,
            "nickname", nickname
        ));

        return userDetails;
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

    private String extractProviderUserId(Provider provider, Map<String, Object> attributes,
        OAuth2UserRequest userRequest) {
        return switch (provider) {
            case GOOGLE -> getString(attributes, "sub");
            case KAKAO -> getString(attributes, "id");
            default -> getString(attributes,
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                    .getUserNameAttributeName());
        };
    }

    private String extractEmail(Provider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> getString(attributes, "email");
            case KAKAO -> {
                Object account = attributes.get("kakao_account");
                yield account instanceof Map<?, ?> acc ? getString(acc, "email") : null;
            }
            default -> null;
        };
    }

    private String extractNickname(Provider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> getString(attributes, "name");
            case KAKAO -> {
                Object properties = attributes.get("properties");
                yield properties instanceof Map<?, ?> map ? getString(map, "nickname") : null;
            }
            default -> null;
        };
    }

    private String getString(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}
