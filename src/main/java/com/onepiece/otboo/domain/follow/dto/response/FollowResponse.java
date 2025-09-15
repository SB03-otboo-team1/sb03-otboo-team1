package com.onepiece.otboo.domain.follow.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FollowResponse {
    private UUID id;
    private UUID followerId;
    private UUID followingId;
    private Instant createdAt;
}