package com.onepiece.otboo.domain.feed.fixture;

import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import java.util.UUID;

public class AuthorDtoFixture {

    public static AuthorDto createAuthorDto(UUID userId, String name) {
        return AuthorDto.builder()
            .userId(userId)
            .name(name)
            .profileImageUrl(name + ".png")
            .build();
    }
}
