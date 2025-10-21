package com.onepiece.otboo.global.event.listener;

import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.service.NotificationService;
import com.onepiece.otboo.global.event.event.ClothesAttributeAddedEvent;
import com.onepiece.otboo.global.event.event.DirectMessageCreatedEvent;
import com.onepiece.otboo.global.event.event.FeedCommentCreatedEvent;
import com.onepiece.otboo.global.event.event.FeedLikedEvent;
import com.onepiece.otboo.global.event.event.FollowCreatedEvent;
import com.onepiece.otboo.global.event.event.RoleUpdatedEvent;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * 1. DM 생성 알림
     */
    @EventListener
    public void handleDirectMessageCreated(DirectMessageCreatedEvent event) {
        var dto = event.data();

        notificationService.create(
            Set.of(dto.receiver().userId()),
            "새로운 메시지",
            dto.sender().name() + "님이 메시지를 보냈습니다.",
            Level.INFO
        );

        log.info("[NotificationEventListener] DM 알림 전송 완료 - receiverId: {}",
            dto.receiver().userId());
    }

    /**
     * 2. 팔로우 생성 알림
     */
    @EventListener
    public void handleFollowCreated(FollowCreatedEvent event) {
        var dto = event.data();

        notificationService.create(
            Set.of(dto.followee().userId()),
            "새로운 팔로워",
            dto.follower().name() + "님이 나를 팔로우했습니다.",
            Level.INFO
        );

        log.info("[NotificationEventListener] 팔로우 알림 전송 완료 - followeeId: {}",
            dto.followee().userId());
    }

    /**
     * 3. 피드 좋아요 알림
     */
    @EventListener
    public void handleFeedLiked(FeedLikedEvent event) {
        var dto = event.data();
        notificationService.create(
            Set.of(dto.author().userId()),
            "피드 좋아요",
            "누군가 당신의 피드를 좋아했습니다.",
            Level.INFO
        );
        log.info("[NotificationEventListener] 피드 좋아요 알림 전송 완료 - authorId: {}",
            dto.author().userId());
    }

    /**
     * 4. 피드 댓글 알림
     */
    @EventListener
    public void handleFeedCommentCreated(FeedCommentCreatedEvent event) {
        var dto = event.data();

        notificationService.create(
            Set.of(dto.feedId()),
            "새 댓글",
            dto.author().name() + "님이 당신의 피드에 댓글을 남겼습니다.",
            Level.INFO
        );
        log.info("[NotificationEventListener] 댓글 알림 전송 완료 - feedId: {}", dto.feedId());
    }

    /**
     * 5. 의상 속성 추가 알림
     */
    @EventListener
    public void handleClothesAttributeAdded(ClothesAttributeAddedEvent event) {
        notificationService.create(
            Set.of(event.userId()),
            "의상 속성 추가",
            "새로운 의상 속성이 추가되었습니다: " + event.data().name(),
            Level.INFO
        );
        log.info("[NotificationEventListener] 의상 속성 알림 전송 완료 - userId: {}", event.userId());
    }

    /**
     * 6. 권한 변경 알림
     */
    @EventListener
    public void handleRoleUpdated(RoleUpdatedEvent event) {
        var dto = event.data();
        notificationService.create(
            Set.of(dto.id()),
            "권한 변경",
            "당신의 권한이 '" + dto.role() + "'로 변경되었습니다.",
            Level.WARNING
        );
        log.info("[NotificationEventListener] 권한 변경 알림 전송 완료 - userId: {}", dto.id());
    }
}