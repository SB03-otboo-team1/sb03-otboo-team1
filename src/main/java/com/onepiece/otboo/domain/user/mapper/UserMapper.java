package com.onepiece.otboo.domain.user.mapper;

import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // User + Profile → UserDto
    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "profile.nickname")  // Profile.nickname 매핑
    @Mapping(target = "role", source = "user.role")
    @Mapping(target = "linkedOAuthProviders", expression = "java(toProviders(user))")
    @Mapping(target = "locked", source = "user.locked")
    UserDto toDto(User user, Profile profile);

    // 필요 시 Provider 리스트 변환 로직
    default List<Provider> toProviders(User user) {
        return List.of(user.getSocialAccount().getProvider()); // 단일 Provider만 갖는 경우
    }
}
