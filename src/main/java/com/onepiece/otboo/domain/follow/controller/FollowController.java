package com.onepiece.otboo.domain.follow.controller;

import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.domain.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * 팔로우 생성
     */
    @PostMapping
    public ResponseEntity<FollowResponse> createFollow(@RequestBody FollowRequest request) {
        FollowResponse response = followService.createFollow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 팔로워 목록 조회
     */
    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<FollowResponse>> getFollowers(@PathVariable UUID userId) {
        List<FollowResponse> responses = followService.getFollowers(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 팔로잉 목록 조회 (프로필 포함)
     */
    @GetMapping("/followings/{userId}")
    public ResponseEntity<List<FollowingResponse>> getFollowings(@PathVariable UUID userId) {
        List<FollowingResponse> responses = followService.getFollowings(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 언팔로우(팔로우 취소)
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteFollow(@RequestBody FollowRequest request) {
        followService.deleteFollow(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 팔로우 요약 정보 조회
     */
    @GetMapping("/summary/{userId}")
    public ResponseEntity<FollowSummaryResponse> getFollowSummary(
        @PathVariable UUID userId,
        @RequestParam(required = false) UUID viewerId) {
        FollowSummaryResponse summary = followService.getFollowSummary(userId, viewerId);
        return ResponseEntity.ok(summary);
    }
}