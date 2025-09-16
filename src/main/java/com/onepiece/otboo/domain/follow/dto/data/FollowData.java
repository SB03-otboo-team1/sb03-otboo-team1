package com.onepiece.otboo.domain.follow.dto.data;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor

public class FollowData {
    private UUID id;
    private UUID followerId;
    private UUID followingId;
}