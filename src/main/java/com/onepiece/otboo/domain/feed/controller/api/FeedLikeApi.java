package com.onepiece.otboo.domain.feed.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.UUID;

@Tag(name = "피드 관리", description = "피드 관련 API")
public interface FeedLikeApi {

    @Operation(summary = "피드 좋아요", description = "지정한 피드를 좋아요합니다.")
    @ApiResponse(responseCode = "204", description = "피드 좋아요 성공")
    @PostMapping("/api/feeds/{feedId}/like")
    ResponseEntity<Void> like(@PathVariable("feedId") UUID feedId);

    @Operation(summary = "피드 좋아요 취소", description = "지정한 피드의 좋아요를 취소합니다.")
    @ApiResponse(responseCode = "204", description = "피드 좋아요 취소 성공")
    @DeleteMapping("/api/feeds/{feedId}/like")
    ResponseEntity<Void> unlike(@PathVariable("feedId") UUID feedId);
}
