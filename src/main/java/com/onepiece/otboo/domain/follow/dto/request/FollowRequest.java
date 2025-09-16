package com.onepiece.otboo.domain.follow.dto.request;


import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class FollowRequest {
    private UUID followerId;
    private UUID followingId;
}