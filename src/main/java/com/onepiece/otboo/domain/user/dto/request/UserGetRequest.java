package com.onepiece.otboo.domain.user.dto.request;

import com.onepiece.otboo.domain.user.enums.Role;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record UserGetRequest(
    String cursor,
    String idAfter,
    @Min(0)
    Integer limit,
    @Pattern(regexp = "email|createdAt", message = "정렬 조건은 email과 createAt만 가능합니다.")
    String sortBy,
    @Pattern(regexp = "ASCENDING|DESCENDING", message = "정렬 방향은 ASCENDING과 DESCENDING만 가능합니다.")
    String sortDirection,
    String emailLike,
    Role roleEqual,
    Boolean locked
) {

    public UserGetRequest {
        if (limit == null) {
            limit = 20;
        }
        if (sortBy == null) {
            sortBy = "createdAt";
        }
        if (sortDirection == null) {
            sortDirection = "DESCENDING";
        }
    }
}
