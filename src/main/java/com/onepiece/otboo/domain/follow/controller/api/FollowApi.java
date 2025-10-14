package com.onepiece.otboo.domain.follow.controller.api;

import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryDto;
import com.onepiece.otboo.domain.follow.dto.response.FollowerResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Follow API", description = "팔로우 관련 API")
@RequestMapping("/api/follows")
public interface FollowApi {

    @Operation(summary = "팔로우 생성", description = "사용자가 다른 사용자를 팔로우합니다.")
    ResponseEntity<FollowResponse> createFollow(FollowRequest request);

    @Operation(summary = "팔로워 목록 조회", description = "특정 사용자를 팔로우하는 모든 사용자 목록을 조회합니다.")
    ResponseEntity<CursorPageResponseDto<FollowerResponse>> getFollowers(
        UUID followeeId,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike,
        SortBy sortBy,
        SortDirection sortDirection
    );

    @Operation(summary = "팔로잉 목록 조회", description = "특정 사용자가 팔로우하는 사용자 목록을 조회합니다.")
    ResponseEntity<CursorPageResponseDto<FollowingResponse>> getFollowings(
        UUID followerId,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike,
        SortBy sortBy,
        SortDirection sortDirection
    );

    @Operation(summary = "언팔로우", description = "사용자가 특정 사용자를 언팔로우합니다.")
    ResponseEntity<Void> deleteFollow(FollowRequest request);

    @Operation(summary = "팔로우 요약 조회", description = "팔로워 수, 팔로잉 수, viewer 기준 팔로우 여부를 반환합니다.")
    ResponseEntity<FollowSummaryDto> getFollowSummary(UUID userId);
}