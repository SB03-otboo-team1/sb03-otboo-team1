package com.onepiece.otboo.global.event.event;

import com.onepiece.otboo.domain.user.dto.response.UserDto;
import java.time.Instant;

public record RoleUpdatedEvent(
    UserDto data,
    Instant createdAt
) {

}