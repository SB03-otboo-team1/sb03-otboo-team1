package com.onepiece.otboo.global.dto.response;

import com.onepiece.otboo.global.enums.SortDirection;
import java.util.List;
import java.util.UUID;

public record CursorPageResponseDto<T>(
    List<T> data,
    String nextCursor,
    UUID nextIdAfter, // swagger UI 명세대로 바꿈
    Boolean hasNext,
    Long totalCount,
    String sortBy,
    SortDirection sortDirection
) {

}
