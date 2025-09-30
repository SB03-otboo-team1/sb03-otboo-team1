package com.onepiece.otboo.domain.feed.controller;

import com.onepiece.otboo.domain.feed.controller.api.FeedApi;
import com.onepiece.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.onepiece.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.service.FeedService;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PatchMapping("/{feedId}")
    @Override
    public ResponseEntity<FeedResponse> updateFeed(@PathVariable UUID feedId,
                                                   @Valid @RequestBody FeedUpdateRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated");
        }

        UUID requesterId = resolveRequesterId(auth);

        FeedResponse res = feedService.update(feedId, requesterId, req);
        return ResponseEntity.ok(res);
    }

    private UUID resolveRequesterId(Authentication auth) {
        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails cud) {
            return cud.getUserId();
        }
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            throw new GlobalException(ErrorCode.FEED_FORBIDDEN);
        }
    }
}
