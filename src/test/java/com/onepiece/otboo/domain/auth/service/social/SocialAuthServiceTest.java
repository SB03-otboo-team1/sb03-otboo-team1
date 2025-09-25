package com.onepiece.otboo.domain.auth.service.social;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class SocialAuthServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    SocialAuthService service;

    final String email = "test@social.com";
    final Provider provider = Provider.GOOGLE;
    final String providerUserId = "google-12345";

    @Test
    void 소셜_정보로_기존_유저_존재시_그대로_반환() {
        // given
        User existing = UserFixture.createSocialUser(email, "", false, Role.USER, provider,
            providerUserId);
        given(userRepository.findBySocialAccountProviderAndSocialAccountProviderUserId(provider,
            providerUserId))
            .willReturn(Optional.of(existing));

        // when
        User result = service.linkOrCreateUser(provider, providerUserId, email);

        // then
        assertThat(result).isSameAs(existing);
        verify(userRepository).findBySocialAccountProviderAndSocialAccountProviderUserId(provider,
            providerUserId);
    }

    @Test
    void email로_기존_유저_존재시_소셜계정_연동_후_저장() {
        // given
        given(userRepository.findBySocialAccountProviderAndSocialAccountProviderUserId(provider,
            providerUserId))
            .willReturn(Optional.empty());
        User userByEmail = UserFixture.createUser(email);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(userByEmail));
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        User result = service.linkOrCreateUser(provider, providerUserId, email);

        // then
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getSocialAccount().getProvider()).isEqualTo(provider);
        assertThat(result.getSocialAccount().getProviderUserId()).isEqualTo(providerUserId);
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 소셜_정보와_email_모두_없으면_신규_유저_생성() {
        // given
        given(userRepository.findBySocialAccountProviderAndSocialAccountProviderUserId(provider,
            providerUserId))
            .willReturn(Optional.empty());
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        User result = service.linkOrCreateUser(provider, providerUserId, email);

        // then
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getSocialAccount().getProvider()).isEqualTo(provider);
        assertThat(result.getSocialAccount().getProviderUserId()).isEqualTo(providerUserId);
        assertThat(result.getPassword()).isEqualTo("");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 소셜_가입_후_비밀번호_초기화_및_일반_로그인() {
        // given
        User socialUser = UserFixture.createSocialUser(email, "", false, Role.USER, provider,
            providerUserId);

        // when
        String updatedPassword = "newPassword123!";
        socialUser.updatePassword(updatedPassword);

        // then
        assertThat(socialUser.getPassword()).isEqualTo(updatedPassword);
    }
}
