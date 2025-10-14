package com.onepiece.otboo.domain.follow.mapper;

import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.follow.dto.response.FollowDto;
import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.global.storage.S3Storage;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "followee", expression = "java(toAuthorDto(follow.getFollowing(), storage))")
    @Mapping(target = "follower", expression = "java(toAuthorDto(follow.getFollower(), storage))")
    FollowDto toDto(Follow follow, @Context FileStorage storage);

    // User -> AuthorDto
    default AuthorDto toAuthorDto(User user, @Context FileStorage storage) {
        return new AuthorDto(
            user.getId(),
            user.getNickname(),
            toPublicUrl(user.getProfileImage(), storage)
        );
    }

    @Named("toPublicUrl")
    default String toPublicUrl(String key, @Context FileStorage storage) {
        if (key == null) {
            return null;
        }
        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }
        if (storage instanceof S3Storage s3) {
            return s3.generatePresignedUrl(key);
        }
        return key;
    }
}