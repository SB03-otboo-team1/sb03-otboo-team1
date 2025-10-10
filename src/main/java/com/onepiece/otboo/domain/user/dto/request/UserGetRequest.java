package com.onepiece.otboo.domain.user.dto.request;

import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserGetRequest(
    String cursor,
    UUID idAfter,
    @Min(1)
    @Max(100)
    Integer limit,
    SortBy sortBy,
    SortDirection sortDirection,
    String emailLike,
    Role roleEqual,
    Boolean locked
) {


    public UserGetRequest {
        if (limit == null) {
            limit = 20;
        }
        if (sortBy == null) {
            sortBy = SortBy.CREATED_AT;
        }
        if (sortDirection == null) {
            sortDirection = SortDirection.DESCENDING;
        }
    }

    public boolean sortByCreatedAt() {
        return SortBy.CREATED_AT.equals(sortBy);
    }

    public boolean isAscending() {
        return sortDirection.equals(SortDirection.ASCENDING);
    }

    public SortBy sortByEnum() {
        return sortByCreatedAt() ? SortBy.CREATED_AT : SortBy.EMAIL;
    }

    public static Instant parseCreatedAtStrict(String s) {
        try {
            return Instant.parse(s);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("createdAt 커서가 유효한 ISO-8601 형식이 아닙니다.", e);
        }
    }
}
