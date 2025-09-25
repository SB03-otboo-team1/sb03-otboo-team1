package com.onepiece.otboo.domain.follow.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
public class FollowingResponse {
    private UUID id;
    private UUID followingId;
    private String nickname;
    private String profileImage;
    private Instant createdAt;

    @QueryProjection
    public FollowingResponse(UUID id, UUID followingId, String nickname, String profileImage, Instant createdAt) {
        this.id = id;
        this.followingId = followingId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.createdAt = createdAt;
    }
}