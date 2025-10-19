package com.onepiece.otboo.domain.dm.fixture;

import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import java.util.UUID;

public class DirectMessageDtoFixture {

    public static DirectMessageDto createDirectMessageDto(AuthorDto sender, AuthorDto receiver,
        String content) {
        return DirectMessageDto.builder()
            .id(UUID.randomUUID())
            .sender(sender)
            .receiver(receiver)
            .content(content)
            .build();
    }
}
