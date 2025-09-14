package com.onepiece.otboo.domain.user.dto.response;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserDto(
    UUID id,
    Instant createdAt,
    String email,
    Role role,
    List<Provider> linkedOAuthProviders,
    boolean locked
) {

    public static UserDto from(User user) {
        return new UserDto(
            user.getId(),
            user.getCreatedAt(),
            user.getEmail(),
            user.getRole(),
            List.of(user.getProvider()),
            user.isLocked()
        );
    }
}
