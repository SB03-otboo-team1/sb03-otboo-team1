package com.onepiece.otboo.infra.security.fixture;

import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.util.UUID;

public class UserDetailsFixture {

    public static CustomUserDetails createUser() {
        return new CustomUserDetails(
            UUID.randomUUID(),
            "testuser@email.com",
            "password",
            Role.USER,
            false,
            null,
            null
        );
    }
}
