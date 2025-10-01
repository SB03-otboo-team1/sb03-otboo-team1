package com.onepiece.otboo.domain.user.dto.request;

import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.global.enums.SortDirection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "email|createdAt", message = "정렬 조건은 email과 createdAt만 가능합니다.")
    String sortBy,
    @Pattern(regexp = "ASCENDING|DESCENDING", message = "정렬 방향은 ASCENDING과 DESCENDING만 가능합니다.")
    SortDirection sortDirection,
    String emailLike,
    Role roleEqual,
    Boolean locked
) {

    public enum SortBy {EMAIL, CREATED_AT}

    public UserGetRequest {
        if (limit == null) {
            limit = 20;
        }
        if (sortBy == null) {
            sortBy = "createdAt";
        }
        if (sortDirection == null) {
            sortDirection = SortDirection.DESCENDING;
        }
    }

    public boolean sortByCreatedAt() {
        return "createdAt".equals(sortBy);
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
