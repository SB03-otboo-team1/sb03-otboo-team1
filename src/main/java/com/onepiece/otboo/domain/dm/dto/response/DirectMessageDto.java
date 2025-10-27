package com.onepiece.otboo.domain.dm.dto.response;

import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DirectMessageDto(
    UUID id,
    Instant createdAt,
    AuthorDto sender,
    AuthorDto receiver,
    String content
) {

}