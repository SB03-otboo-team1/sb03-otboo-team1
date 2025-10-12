package com.onepiece.otboo.domain.user.fixture;

import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.enums.Role;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class UserDtoFixture {

    public static UserDto createUser(
        String email,
        String name,
        Role role,
        boolean locked
    ) {
        return UserDto.builder()
            .id(UUID.randomUUID())
            .createdAt(Instant.now())
            .email(email)
            .name(name)
            .role(role)
            .linkedOAuthProviders(List.of())
            .locked(locked)
            .build();
    }

    public static List<UserDto> createDummyUsers() {
        return List.of(
            createUser("han@test.com", "한유리", Role.USER, false),
            createUser("kim@test.com", "김철수", Role.USER, true),
            createUser("lee@test.com", "이훈이", Role.USER, false),
            createUser("maeng@test.com", "맹구", Role.USER, true),
            createUser("shin@test.com", "신짱구", Role.ADMIN, false)
        );
    }
}
