package com.onepiece.otboo.domain.feed.controller;

import com.onepiece.otboo.domain.feed.controller.api.FeedLikeApi;
import com.onepiece.otboo.domain.feed.service.FeedLikeService;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FeedLikeController implements FeedLikeApi {

    private final FeedLikeService feedLikeService;
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<Void> like(UUID feedId) {
        feedLikeService.like(feedId, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> unlike(UUID feedId) {
        feedLikeService.unlike(feedId, currentUserId());
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }
        // Spring Security의 Principal에서 직접 사용자 정보 가져오기
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            // CustomUserDetails에 userId 필드가 있다고 가정
            return ((CustomUserDetails) userDetails).getUserId();
            }
        throw new AccessDeniedException("올바르지 않은 인증 정보입니다.");
    }
}
