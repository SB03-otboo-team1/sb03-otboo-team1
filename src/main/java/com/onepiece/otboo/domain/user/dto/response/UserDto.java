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
    Role role,
    List<Provider> linkedOAuthProviders,
    boolean locked
) {

}
