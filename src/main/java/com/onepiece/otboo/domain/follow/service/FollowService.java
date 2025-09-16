package com.onepiece.otboo.domain.follow.service;

import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;

import java.util.List;
import java.util.UUID;

public interface FollowService {

    FollowResponse createFollow(FollowRequest request);

    List<FollowResponse> getFollowers(UUID userId);

    List<FollowResponse> getFollowings(UUID userId);

    void deleteFollow(FollowRequest request);

}