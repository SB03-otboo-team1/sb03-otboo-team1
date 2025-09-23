package com.onepiece.otboo.domain.user.repository;

import com.onepiece.otboo.domain.user.dto.request.UserGetRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.enums.Role;
import java.util.List;

public interface UserRepositoryCustom {

    List<UserDto> findUsers(UserGetRequest userGetRequest);

    long countUsers(String emailLike, Role roleEqual, Boolean locked);
}
