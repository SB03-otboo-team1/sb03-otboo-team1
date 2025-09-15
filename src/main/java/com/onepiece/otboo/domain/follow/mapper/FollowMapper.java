package com.onepiece.otboo.domain.follow.mapper;

import com.onepiece.otboo.domain.follow.dto.data.FollowData;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.entity.Follow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    @Mapping(source = "follower.id", target = "followerId")
    @Mapping(source = "following.id", target = "followingId")
    FollowResponse toResponse(Follow follow);

    @Mapping(source = "follower.id", target = "followerId")
    @Mapping(source = "following.id", target = "followingId")
    FollowData toData(Follow follow);

}