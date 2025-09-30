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
public class DirectMessageResponse {

    private UUID id;
    private Instant createdAt;

    private UserInfo sender;
    private UserInfo receiver;

    private String content;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {

        private UUID userId;
        private String email;
    }
}