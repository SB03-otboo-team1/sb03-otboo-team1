package com.onepiece.otboo.domain.feed.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FeedResponse(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    AuthorDto author,
    WeatherDto weather,
    List<OotdDto> ootds,
    String content,
    long likeCount,
    long commentCount,
    boolean likedByMe
) { }
