package com.onepiece.otboo.domain.user.dto.request;

import com.onepiece.otboo.domain.user.enums.Role;

public record UserRoleUpdateRequest(
    Role role
) {

}
