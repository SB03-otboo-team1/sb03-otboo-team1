package com.onepiece.otboo.domain.feed.controller;

import com.onepiece.otboo.domain.feed.controller.api.FeedApi;
import com.onepiece.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.onepiece.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.service.FeedQueryService;
import com.onepiece.otboo.domain.feed.service.FeedService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/feeds")
public class FeedController implements FeedApi {

    private final FeedService feedService;
    private final FeedQueryService feedQueryService;

    // ===== 목록 조회 =====
    @GetMapping(produces = "application/json")
    public ResponseEntity<CursorPageResponseDto<FeedResponse>> listFeeds(
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam int limit,
        @RequestParam SortBy sortBy,
        @RequestParam SortDirection sortDirection,
        @RequestParam(required = false) String keywordLike,
        @RequestParam(required = false) String skyStatusEqual,
        @RequestParam(required = false) String precipitationTypeEqual,
        @RequestParam(required = false) UUID authorIdEqual
    ) {
        UUID me = resolveRequesterIdOrNull(SecurityContextHolder.getContext().getAuthentication());
        var resp = feedQueryService.listFeeds(
            cursor, idAfter, limit, sortBy, sortDirection,
            keywordLike, skyStatusEqual, precipitationTypeEqual, authorIdEqual, me
        );
        return ResponseEntity.ok(resp);
    }

    // ===== 등록 =====
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<FeedResponse> createFeed(@Valid @RequestBody FeedCreateRequest req) {
        FeedResponse res = feedService.create(req);
        return ResponseEntity.created(URI.create("/api/feeds/" + res.id())).body(res);
    }

    // ===== 삭제 =====
    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> deleteFeed(@PathVariable UUID feedId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID requesterId = resolveRequesterId(auth);
        feedService.delete(feedId, requesterId);
        return ResponseEntity.noContent().build();
    }

    // ===== 수정 =====
    @PatchMapping(value = "/{feedId}", consumes = "application/json", produces = "application/json")
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
        if (auth == null) {
            throw new GlobalException(ErrorCode.FEED_FORBIDDEN);
        }
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

    private UUID resolveRequesterIdOrNull(Authentication auth) {
        try {
            return resolveRequesterId(auth);
        } catch (Exception e) {
            return null; // 목록은 익명 허용 → likedByMe만 false 처리
        }
    }
}
