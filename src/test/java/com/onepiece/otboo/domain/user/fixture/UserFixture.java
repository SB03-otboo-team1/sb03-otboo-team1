package com.onepiece.otboo.domain.user.fixture;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;

public class UserFixture {

    public static User createUser() {
        return User.builder()
            .provider(Provider.LOCAL)
            .email("test@example.com")
            .password("password")
            .locked(false)
            .role(Role.USER)
            .build();
    }
}