package com.onepiece.otboo.domain.feed.controller;

import com.onepiece.otboo.domain.feed.controller.api.FeedLikeApi;
import com.onepiece.otboo.domain.feed.service.FeedLikeService;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feeds")
public class FeedLikeController implements FeedLikeApi {

    private final FeedLikeService feedLikeService;

    @PostMapping("/{feedId}/like")
    @Override
    public ResponseEntity<Void> like(@PathVariable UUID feedId,
        @AuthenticationPrincipal CustomUserDetails user) {
        feedLikeService.like(user.getUserId(), feedId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{feedId}/like")
    @Override
    public ResponseEntity<Void> unlike(@PathVariable UUID feedId,
        @AuthenticationPrincipal CustomUserDetails user) {
        feedLikeService.unlike(user.getUserId(), feedId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{feedId}/like/toggle")
    @Override
    public ResponseEntity<LikeToggleResponse> toggle(@PathVariable UUID feedId,
        @AuthenticationPrincipal CustomUserDetails user) {
        boolean liked = feedLikeService.toggle(user.getUserId(), feedId);
        long count = feedLikeService.countByFeed(feedId);
        return ResponseEntity.ok(new LikeToggleResponse(liked, count));
    }

    @GetMapping("/{feedId}/likes/count")
    @Override
    public ResponseEntity<CountResponse> count(@PathVariable UUID feedId) {
        return ResponseEntity.ok(new CountResponse(feedLikeService.countByFeed(feedId)));
    }
}
