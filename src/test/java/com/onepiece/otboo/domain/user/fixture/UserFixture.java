package com.onepiece.otboo.domain.user.fixture;

import com.onepiece.otboo.domain.user.entity.SocialAccount;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import java.time.Instant;

public class UserFixture {

    public static User createUser() {
        return createSocialUser("test@example.com", "password", false, Role.USER, Provider.LOCAL,
            "local-1");
    }

    public static User createUser(String email) {
        return createSocialUser(email, "password", false, Role.USER, Provider.LOCAL, "local-1");
    }

    public static User createSocialUser(String email, String password, boolean locked, Role role,
        Provider provider, String providerUserId) {
        return User.builder()
            .email(email)
            .password(password)
            .locked(locked)
            .role(role)
            .socialAccount(SocialAccount.builder()
                .provider(provider)
                .providerUserId(providerUserId)
                .build())
            .build();
    }

    public static User createUserWithTemporaryPassword(String tempPassword,
        Instant expiration) {
        return User.builder()
            .email("test@example.com")
            .password("password")
            .temporaryPassword(tempPassword)
            .temporaryPasswordExpirationTime(expiration)
            .locked(false)
            .role(Role.USER)
            .socialAccount(SocialAccount.builder()
                .provider(Provider.LOCAL)
                .providerUserId("local-1")
                .build())
            .build();
    }
}
