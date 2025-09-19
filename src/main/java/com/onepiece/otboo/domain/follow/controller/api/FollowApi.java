package com.onepiece.otboo.domain.follow.controller.api;

import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Follow API", description = "팔로우 관련 API")
@RequestMapping("/api/follows")
public interface FollowApi {

    @Operation(
        summary = "팔로우 생성",
        description = "사용자가 다른 사용자를 팔로우합니다.",
        responses = {
            @ApiResponse(responseCode = "201", description = "팔로우 생성 성공",
                content = @Content(schema = @Schema(implementation = FollowResponse.class))),
            @ApiResponse(responseCode = "400", description = "이미 팔로우 관계 존재 또는 잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "대상 사용자 없음")
        }
    )
    @PostMapping
    ResponseEntity<FollowResponse> createFollow(@RequestBody FollowRequest request);

    @Operation(
        summary = "팔로워 목록 조회",
        description = "특정 사용자를 팔로우하는 모든 사용자 목록을 조회합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = FollowResponse.class))),
            @ApiResponse(responseCode = "404", description = "대상 사용자 없음")
        }
    )
    @GetMapping("/followers/{userId}")
    ResponseEntity<List<FollowResponse>> getFollowers(@PathVariable UUID userId);

    @Operation(
        summary = "팔로잉 목록 조회",
        description = "특정 사용자가 팔로우하는 사용자 목록을 조회합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = FollowResponse.class))),
            @ApiResponse(responseCode = "404", description = "대상 사용자 없음")
        }
    )
    @GetMapping("/followings/{userId}")
    ResponseEntity<List<FollowResponse>> getFollowings(@PathVariable UUID userId);

    @Operation(
        summary = "언팔로우",
        description = "사용자가 특정 사용자를 언팔로우합니다.",
        responses = {
            @ApiResponse(responseCode = "204", description = "언팔로우 성공"),
            @ApiResponse(responseCode = "404", description = "대상 사용자 없음")
        }
    )
    @DeleteMapping
    ResponseEntity<Void> deleteFollow(@RequestBody FollowRequest request);

    @Operation(
        summary = "팔로우 요약 조회",
        description = "특정 사용자의 팔로워 수, 팔로잉 수, viewer 기준 팔로우 여부를 반환합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = FollowSummaryResponse.class))),
            @ApiResponse(responseCode = "404", description = "대상 사용자 없음")
        }
    )
    @GetMapping("/summary/{userId}")
    ResponseEntity<FollowSummaryResponse> getFollowSummary(
        @PathVariable UUID userId,
        @RequestParam(required = false) UUID viewerId
    );
}