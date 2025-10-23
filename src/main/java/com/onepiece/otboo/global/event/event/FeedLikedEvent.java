package com.onepiece.otboo.global.event.event;

import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import java.time.Instant;

public record FeedLikedEvent(
    FeedResponse feed,
    UserDto liker,
    Instant createdAt
) {

}