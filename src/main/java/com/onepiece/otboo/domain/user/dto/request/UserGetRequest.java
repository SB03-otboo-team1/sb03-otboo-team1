package com.onepiece.otboo.domain.user.dto.request;

import com.onepiece.otboo.domain.user.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserGetRequest(
    String cursor,
    String idAfter,
    @NotNull
    Integer limit,
    @NotNull
    String sortBy,
    @NotNull
    String sortDirection,
    String emailLike,
    Role roleEqual,
    Boolean locked
) {

}
