package com.onepiece.otboo.domain.follow.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FollowingResponse {
    private UUID id;
    private UUID followingId;
    private String nickname;
    private String profileImage;
    private Instant createdAt;
}
