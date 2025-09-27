package com.onepiece.otboo.domain.follow.mapper;

import com.onepiece.otboo.domain.follow.dto.data.FollowData;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.entity.Follow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    /**
     * Follow → FollowResponse
     * 닉네임/프로필이미지는 Repository Projection에서 채워줌
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "follower.id", target = "followerId")
    @Mapping(source = "createdAt", target = "createdAt")
    FollowResponse toResponse(Follow follow);

    /**
     * Follow → FollowData (단순 식별자 정보만)
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "follower.id", target = "followerId")
    @Mapping(source = "following.id", target = "followingId")
    FollowData toData(Follow follow);
}