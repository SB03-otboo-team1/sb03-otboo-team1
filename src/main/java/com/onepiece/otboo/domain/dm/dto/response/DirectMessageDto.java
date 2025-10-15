package com.onepiece.otboo.domain.dm.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DirectMessageDto {

    private UUID id;
    private Instant createdAt;
    private UserDto sender;
    private UserDto receiver;
    private String content;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDto {

        private UUID id;
        private String email;
    }
}