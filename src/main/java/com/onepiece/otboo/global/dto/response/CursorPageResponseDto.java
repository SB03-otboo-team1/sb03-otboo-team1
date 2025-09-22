package com.onepiece.otboo.global.dto.response;

import java.util.List;

public record CursorPageResponseDto<T>(
    List<T> data,
    String nextCursor,
    String nextIdAfter,
    Boolean hasNext,
    Long totalCount,
    String sortBy,
    String sortDirection
) {

}
