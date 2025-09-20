package com.onepiece.otboo.domain.feed.controller;

import com.onepiece.otboo.domain.feed.controller.api.FeedApi;
import com.onepiece.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.service.FeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController implements FeedApi {
    private final FeedService feedService;

    @PostMapping
    public ResponseEntity<FeedResponse> createFeed(@Valid @RequestBody FeedCreateRequest req) {
        FeedResponse res = feedService.create(req);
        return ResponseEntity.created(URI.create("/api/feeds/" + res.id())).body(res);
    }


    @Override
    public ResponseEntity<Void> deleteFeed(@PathVariable UUID feedId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID requesterId = UUID.fromString(auth.getName());

        feedService.delete(feedId, requesterId);
        return ResponseEntity.noContent().build(); // 204
    }
}
