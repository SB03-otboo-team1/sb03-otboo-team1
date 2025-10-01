package com.onepiece.otboo.domain.follow.controller;

import com.onepiece.otboo.domain.follow.controller.api.FollowApi;
import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowerResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.domain.follow.service.FollowService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortDirection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FollowController implements FollowApi {

    private final FollowService followService;

    @Override
    public ResponseEntity<FollowResponse> createFollow(FollowRequest request) {
        FollowResponse response = followService.createFollow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<CursorPageResponseDto<FollowerResponse>> getFollowers(
        UUID followeeId,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike
    ) {
        String sortBy = "createdAt";
        SortDirection sortDirection = SortDirection.ASCENDING;
        CursorPageResponseDto<FollowerResponse> responses =
            followService.getFollowers(followeeId, cursor, idAfter, limit, nameLike, sortBy, sortDirection);
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<CursorPageResponseDto<FollowingResponse>> getFollowings(
        UUID followerId,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike
    ) {
        String sortBy = "createdAt";
        SortDirection sortDirection = SortDirection.ASCENDING;
        CursorPageResponseDto<FollowingResponse> responses =
            followService.getFollowings(followerId, cursor, idAfter, limit, nameLike, sortBy, sortDirection);
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<Void> deleteFollow(FollowRequest request) {
        followService.deleteFollow(request);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<FollowSummaryResponse> getFollowSummary(UUID userId, UUID viewerId) {
        FollowSummaryResponse summary = followService.getFollowSummary(userId, viewerId);
        return ResponseEntity.ok(summary);
    }
}