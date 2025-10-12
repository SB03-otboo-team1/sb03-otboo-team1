package com.onepiece.otboo.domain.feed.controller;

import com.onepiece.otboo.domain.feed.controller.api.FeedLikeApi;
import com.onepiece.otboo.domain.feed.service.FeedLikeService;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        String name = auth.getName(); // UUID 또는 email
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException ignore) {
            return userRepository.findByEmail(name)
                .map(u -> u.getId())
                .orElseThrow(() -> new AccessDeniedException("인증 사용자 없음: " + name));
        }
    }
}
