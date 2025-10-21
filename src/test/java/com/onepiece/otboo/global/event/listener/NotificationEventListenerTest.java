package com.onepiece.otboo.global.event.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.comment.dto.response.CommentDto;
import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowDto;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.service.NotificationService;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.global.event.event.ClothesAttributeAddedEvent;
import com.onepiece.otboo.global.event.event.DirectMessageCreatedEvent;
import com.onepiece.otboo.global.event.event.FeedCommentCreatedEvent;
import com.onepiece.otboo.global.event.event.FeedLikedEvent;
import com.onepiece.otboo.global.event.event.FollowCreatedEvent;
import com.onepiece.otboo.global.event.event.RoleUpdatedEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener listener;

    @DisplayName("DM 생성 이벤트 발생 시 알림이 전송된다")
    @Test
    void handleDirectMessageCreated() {
        var sender = new AuthorDto(UUID.randomUUID(), "보내는사람", null);
        var receiver = new AuthorDto(UUID.randomUUID(), "받는사람", null);

        var dto = new com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto(
            UUID.randomUUID(),
            Instant.now(),
            sender,
            receiver,
            "테스트 메시지입니다."
        );

        listener.handleDirectMessageCreated(new DirectMessageCreatedEvent(dto, Instant.now()));

        verify(notificationService, times(1))
            .create(anySet(), eq("새로운 메시지"), any(String.class), eq(Level.INFO));
    }

    @DisplayName("팔로우 생성 이벤트 발생 시 알림이 전송된다")
    @Test
    void handleFollowCreated() {
        var follower = new AuthorDto(UUID.randomUUID(), "팔로워", null);
        var followee = new AuthorDto(UUID.randomUUID(), "팔로이", null);
        var dto = new FollowDto(UUID.randomUUID(), followee, follower);

        listener.handleFollowCreated(new FollowCreatedEvent(dto, Instant.now()));

        verify(notificationService, times(1))
            .create(anySet(), eq("새로운 팔로워"), any(String.class), eq(Level.INFO));
    }

    @DisplayName("피드 좋아요 이벤트 발생 시 알림이 전송된다")
    @Test
    void handleFeedLiked() {
        var author = new AuthorDto(UUID.randomUUID(), "작성자", null);
        var feed = new FeedResponse(
            UUID.randomUUID(),
            Instant.now(),
            Instant.now(),
            author,
            null,
            List.of(),
            "테스트 피드 내용",
            0,
            0,
            false
        );

        listener.handleFeedLiked(new FeedLikedEvent(feed, Instant.now()));

        verify(notificationService, times(1))
            .create(anySet(), eq("피드 좋아요"), any(String.class), eq(Level.INFO));
    }

    @DisplayName("피드 댓글 이벤트 발생 시 알림이 전송된다")
    @Test
    void handleFeedCommentCreated() {
        var author = new AuthorDto(UUID.randomUUID(), "댓글작성자", null);
        var comment = new CommentDto(
            UUID.randomUUID(),
            Instant.now(),
            UUID.randomUUID(),
            author,
            "댓글 내용입니다."
        );

        listener.handleFeedCommentCreated(new FeedCommentCreatedEvent(comment, Instant.now()));

        verify(notificationService, times(1))
            .create(anySet(), eq("새 댓글"), any(String.class), eq(Level.INFO));
    }

    @DisplayName("의상 속성 추가 이벤트 발생 시 알림이 전송된다")
    @Test
    void handleClothesAttributeAdded() {
        var request = new com.onepiece.otboo.domain.clothes.dto.request.ClothesAttributeDefCreateRequest(
            "소재", List.of("면", "린넨")
        );

        listener.handleClothesAttributeAdded(
            new ClothesAttributeAddedEvent(UUID.randomUUID(), request, Instant.now()));

        verify(notificationService, times(1))
            .create(anySet(), eq("의상 속성 추가"), any(String.class), eq(Level.INFO));
    }

    @DisplayName("권한 변경 이벤트 발생 시 알림이 전송된다")
    @Test
    void handleRoleUpdated() {
        var user = UserDto.builder()
            .id(UUID.randomUUID())
            .email("test@otboo.com")
            .role(Role.USER)
            .build();

        listener.handleRoleUpdated(new RoleUpdatedEvent(user, Instant.now()));

        verify(notificationService, times(1))
            .create(anySet(), eq("권한 변경"), any(String.class), eq(Level.WARNING));
    }
}