package com.onepiece.otboo.domain.follow.controller;

import com.onepiece.otboo.domain.follow.controller.api.FollowApi;
import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryDto;
import com.onepiece.otboo.domain.follow.dto.response.FollowerResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.domain.follow.service.FollowService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FollowController implements FollowApi {

    private final FollowService followService;

    @Override
    @PostMapping
    public ResponseEntity<FollowResponse> createFollow(@RequestBody FollowRequest request) {
        FollowResponse response = followService.createFollow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/followers/{userId}")
    public ResponseEntity<CursorPageResponseDto<FollowerResponse>> getFollowers(
        @PathVariable("userId") UUID followeeId,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String nameLike,
        @RequestParam(defaultValue = "CREATED_AT") SortBy sortBy,
        @RequestParam(defaultValue = "ASCENDING") SortDirection sortDirection
    ) {
        CursorPageResponseDto<FollowerResponse> responses =
            followService.getFollowers(followeeId, cursor, idAfter, limit, nameLike, sortBy,
                sortDirection);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/followings/{userId}")
    public ResponseEntity<CursorPageResponseDto<FollowingResponse>> getFollowings(
        @PathVariable("userId") UUID followerId,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String nameLike,
        @RequestParam(defaultValue = "CREATED_AT") SortBy sortBy,
        @RequestParam(defaultValue = "ASCENDING") SortDirection sortDirection
    ) {
        CursorPageResponseDto<FollowingResponse> responses =
            followService.getFollowings(followerId, cursor, idAfter, limit, nameLike, sortBy,
                sortDirection);
        return ResponseEntity.ok(responses);
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deleteFollow(FollowRequest request) {
        followService.deleteFollow(request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/summary")
    public ResponseEntity<FollowSummaryDto> getFollowSummary(@RequestParam UUID userId) {
        FollowSummaryDto summary = followService.getFollowSummary(userId);
        return ResponseEntity.ok(summary);
    }
}