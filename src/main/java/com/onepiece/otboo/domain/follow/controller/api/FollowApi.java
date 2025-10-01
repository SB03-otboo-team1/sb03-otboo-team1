package com.onepiece.otboo.domain.follow.controller.api;

import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowerResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Follow API", description = "팔로우 관련 API")
@RequestMapping("/api/follows")
public interface FollowApi {

    @Operation(summary = "팔로우 생성", description = "사용자가 다른 사용자를 팔로우합니다.")
    @PostMapping
    ResponseEntity<FollowResponse> createFollow(@RequestBody FollowRequest request);

    @Operation(summary = "팔로워 목록 조회", description = "특정 사용자를 팔로우하는 모든 사용자 목록을 조회합니다.")
    @GetMapping("/followers/{userId}")
    ResponseEntity<CursorPageResponseDto<FollowerResponse>> getFollowers(
        @PathVariable("userId") UUID followeeId,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String nameLike
    );

    @Operation(summary = "팔로잉 목록 조회", description = "특정 사용자가 팔로우하는 사용자 목록을 조회합니다.")
    @GetMapping("/followings/{userId}")
    ResponseEntity<CursorPageResponseDto<FollowingResponse>> getFollowings(
        @PathVariable("userId") UUID followerId,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String nameLike
    );

    @Operation(summary = "언팔로우", description = "사용자가 특정 사용자를 언팔로우합니다.")
    @DeleteMapping
    ResponseEntity<Void> deleteFollow(@RequestBody FollowRequest request);

    @Operation(summary = "팔로우 요약 조회", description = "팔로워 수, 팔로잉 수, viewer 기준 팔로우 여부를 반환합니다.")
    @GetMapping("/summary/{userId}")
    ResponseEntity<FollowSummaryResponse> getFollowSummary(
        @PathVariable UUID userId,
        @RequestParam(required = false) UUID viewerId
    );
}