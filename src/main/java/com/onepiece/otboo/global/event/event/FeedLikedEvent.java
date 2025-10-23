package com.onepiece.otboo.global.event.event;

import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import java.time.Instant;
import java.util.UUID;

public record FeedLikedEvent(
    FeedResponse feed,
    UUID likerId,
    Instant createdAt
) {

}