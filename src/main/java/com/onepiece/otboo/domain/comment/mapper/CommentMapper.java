package com.onepiece.otboo.domain.comment.mapper;


import com.onepiece.otboo.domain.comment.dto.response.CommentDto;
import com.onepiece.otboo.domain.comment.entity.Comment;
import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "feedId", expression = "java(comment.getFeed().getId())")
    @Mapping(target = "author", expression =
        "java(new AuthorDto(comment.getAuthor().getId(), \"\", null))")
    CommentDto toDto(Comment comment);
}
