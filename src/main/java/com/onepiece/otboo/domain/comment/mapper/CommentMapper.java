package com.onepiece.otboo.domain.comment.mapper;


import com.onepiece.otboo.domain.comment.dto.response.CommentDto;
import com.onepiece.otboo.domain.comment.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "feedId", expression = "java(comment.getFeed().getId())")
    @Mapping(target = "author", expression =
        "java(new AuthorDto(comment.getAuthor().getId(), comment.getAuthor().getNickname(), comment.getAuthor().getProfileImage()))")
    CommentDto toDto(Comment comment);
}
