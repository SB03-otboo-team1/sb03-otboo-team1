package com.onepiece.otboo.domain.follow.dto.request;

import java.util.UUID;

public record FollowRequest(
    UUID followerId,
    UUID followeeId
) {

}
