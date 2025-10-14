package com.onepiece.otboo.domain.feed.controller.api;

import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

public interface FeedLikeApi {

    @Operation(summary = "피드 좋아요")
    @ApiResponse(responseCode = "204", description = "성공(내용 없음)")
    ResponseEntity<Void> like(
        @PathVariable("feedId") UUID feedId,
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
    );

    @Operation(summary = "피드 좋아요 취소")
    @ApiResponse(responseCode = "204", description = "성공(내용 없음)")
    ResponseEntity<Void> unlike(
        @PathVariable("feedId") UUID feedId,
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
    );

    @Operation(summary = "피드 좋아요 토글")
    ResponseEntity<LikeToggleResponse> toggle(
        @PathVariable("feedId") UUID feedId,
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
    );

    @Operation(summary = "피드 좋아요 수 조회")
    ResponseEntity<CountResponse> count(@PathVariable("feedId") UUID feedId);

    record LikeToggleResponse(boolean liked, long count) {}
    record CountResponse(long count) {}
}
