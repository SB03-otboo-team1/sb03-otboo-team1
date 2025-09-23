package com.onepiece.otboo.domain.user.dto.response;

import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserDto(
    UUID id,
    Instant createdAt,
    String email,
    String name,
    Role role,
    List<Provider> linkedOAuthProviders,
    boolean locked
) {

    public UserDto(UUID id, Instant createdAt, String email, String name, Role role,
        Provider provider, boolean locked) {
        this(id, createdAt, email, name, role, List.of(provider), locked);
    }
}
