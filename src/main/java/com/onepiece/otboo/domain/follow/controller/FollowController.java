package com.onepiece.otboo.domain.follow.controller;

import com.onepiece.otboo.domain.follow.controller.api.FollowApi;
import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowDto;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryDto;
import com.onepiece.otboo.domain.follow.service.FollowService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<FollowDto> createFollow(@RequestBody FollowRequest request) {
        FollowDto response = followService.createFollow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/followers")
    public ResponseEntity<CursorPageResponseDto<FollowDto>> getFollowers(
        @RequestParam UUID followeeId,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String nameLike
    ) {
        CursorPageResponseDto<FollowDto> responses =
            followService.getFollowers(followeeId, cursor, idAfter, limit, nameLike);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/followings")
    public ResponseEntity<CursorPageResponseDto<FollowDto>> getFollowings(
        @RequestParam UUID followerId,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String nameLike
    ) {
        CursorPageResponseDto<FollowDto> responses =
            followService.getFollowings(followerId, cursor, idAfter, limit, nameLike);
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