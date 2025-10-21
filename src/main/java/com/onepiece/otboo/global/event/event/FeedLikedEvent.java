package com.onepiece.otboo.global.event.event;

import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import java.time.Instant;

public record FeedLikedEvent(
    FeedResponse data,
    Instant createdAt
) {

}