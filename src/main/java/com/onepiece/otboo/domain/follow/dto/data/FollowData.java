package com.onepiece.otboo.domain.follow.dto.data;

import java.util.UUID;

public record FollowData(
    UUID id,
    UUID followerId,
    UUID followingId
) {

}