package com.onepiece.otboo.domain.feed.dto.response;

import java.util.List;
import java.util.UUID;

public record FeedDtoCursorResponse(
    List<FeedResponse> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    long totalCount,
    String sortBy,
    String sortDirection
) { }