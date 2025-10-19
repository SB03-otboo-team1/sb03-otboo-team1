package com.onepiece.otboo.domain.dm.mapper;

import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import com.onepiece.otboo.domain.dm.entity.DirectMessage;
import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.global.storage.S3Storage;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DirectMessageMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "sender", expression = "java(toAuthorDto(dm.getSender(), storage))")
    @Mapping(target = "receiver", expression = "java(toAuthorDto(dm.getReceiver(), storage))")
    @Mapping(target = "content", source = "content")
    DirectMessageDto toDto(DirectMessage dm, @Context FileStorage storage);

    default AuthorDto toAuthorDto(User user, @Context FileStorage storage) {
        return AuthorDto.builder()
            .userId(user.getId())
            .name(user.getNickname())
            .profileImageUrl(toPublicUrl(user.getProfileImage(), storage))
            .build();
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