package com.onepiece.otboo.infra.security.mapper;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomUserDetailsMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    CustomUserDetails toCustomUserDetails(User user);
}
