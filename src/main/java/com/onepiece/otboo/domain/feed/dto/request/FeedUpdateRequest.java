package com.onepiece.otboo.domain.feed.dto.request;

import jakarta.validation.constraints.Size;

public record FeedUpdateRequest(
    @Size(max = 1000) String content
) {}

