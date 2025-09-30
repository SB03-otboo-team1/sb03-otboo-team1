package com.onepiece.otboo.domain.follow.mapper;

import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.global.storage.S3Storage;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "follower.id", target = "followerId")
    @Mapping(target = "nickname", ignore = true)
    @Mapping(target = "profileImageUrl", ignore = true)
    @Mapping(source = "createdAt", target = "createdAt")
    FollowResponse toResponse(Follow follow, @Context FileStorage storage);

    @Named("toPublicUrl")
    default String toPublicUrl(String key, @Context FileStorage storage) {
        if (key == null) {
            return null;
        }
        if (storage instanceof S3Storage s3) {
            return s3.generatePresignedUrl(key);
        }
        return key;
    }
}