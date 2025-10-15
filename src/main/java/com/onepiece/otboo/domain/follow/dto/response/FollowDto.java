package com.onepiece.otboo.domain.follow.dto.response;

import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import java.util.UUID;
import lombok.Builder;

@Builder
public record FollowDto(
    UUID id,
    AuthorDto followee,
    AuthorDto follower
) {

}