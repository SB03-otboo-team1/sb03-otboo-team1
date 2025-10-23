package com.onepiece.otboo.global.event.event;

import com.onepiece.otboo.domain.follow.dto.response.FollowDto;
import java.time.Instant;

public record FollowCreatedEvent(
    FollowDto data,
    Instant createdAt
) {

}