package com.onepiece.otboo.global.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.feed.repository.FeedRepository;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.service.NotificationService;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.exception.ProfileNotFoundException;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.global.event.event.ClothesAttributeAddedEvent;
import com.onepiece.otboo.global.event.event.DirectMessageCreatedEvent;
import com.onepiece.otboo.global.event.event.FeedCommentCreatedEvent;
import com.onepiece.otboo.global.event.event.FeedLikedEvent;
import com.onepiece.otboo.global.event.event.FollowCreatedEvent;
import com.onepiece.otboo.global.event.event.RoleUpdatedEvent;
import com.onepiece.otboo.global.event.event.WeatherChangeEvent;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredTopicListener {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final ProfileRepository profileRepository;
    private final FeedRepository feedRepository;


    @KafkaListener(
        topics = "otboo.WeatherChangeEvent",
        containerFactory = "processingKafkaListenerContainerFactory"
    )
    public void onWeatherChanged(String kafkaEvent, Acknowledgment ack) {
        handle(
            kafkaEvent, ack,
            "WeatherChangeEvent",
            WeatherChangeEvent.class,
            event -> {
                UUID userId = event.userId();
                String title = event.title();
                String content = event.message();

                notificationService.create(Set.of(userId), title, content, Level.INFO);
                log.debug("[NotificationRequiredTopicListener] 날씨 변화 알림 생성 완료 - userId: {}",
                    userId);
            }
        );
    }

    @KafkaListener(
        topics = "otboo.DirectMessageCreatedEvent",
        containerFactory = "processingKafkaListenerContainerFactory"
    )
    public void onDirectMessageCreated(String kafkaEvent, Acknowledgment ack) {
        handle(
            kafkaEvent, ack,
            "DirectMessageCreatedEvent",
            DirectMessageCreatedEvent.class,
            event -> {
                UUID senderId = event.data().sender().userId();
                UUID receiverId = event.data().receiver().userId();

                Profile sender = findProfile(senderId);
                Profile receiver = findProfile(receiverId);

                String title = sender.getNickname() + "님으로부터 메시지가 왔습니다.";
                String content = event.data().content();

                notificationService.create(Set.of(receiverId), title, content, Level.INFO);
                log.debug(
                    "[NotificationRequiredTopicListener] DM 이벤트 처리 - sender: {}, receiver: {}",
                    sender.getNickname(), receiver.getNickname());
            }
        );
    }

    @KafkaListener(
        topics = "otboo.ClothesAttributeAddedEvent",
        containerFactory = "processingKafkaListenerContainerFactory"
    )
    public void onClothesAttributeAdded(String kafkaEvent, Acknowledgment ack) {
        handle(
            kafkaEvent, ack,
            "ClothesAttributeAddedEvent",
            ClothesAttributeAddedEvent.class,
            event -> {
                UUID userId = event.userId();
                String attributeName = event.data().name();

                notificationService.create(
                    Set.of(userId),
                    "의상 속성 추가",
                    "새로운 의상 속성이 추가되었습니다: " + attributeName,
                    Level.INFO
                );

                log.debug(
                    "[NotificationRequiredTopicListener] ClothesAttributeAddedEvent 처리 완료 - userId: {}, attribute={}",
                    userId, attributeName);
            }
        );
    }

    @KafkaListener(
        topics = "otboo.FeedCommentCreatedEvent",
        containerFactory = "processingKafkaListenerContainerFactory"
    )
    public void onFeedCommentCreated(String kafkaEvent, Acknowledgment ack) {
        handle(
            kafkaEvent, ack,
            "FeedCommentCreatedEvent",
            FeedCommentCreatedEvent.class,
            event -> {
                var dto = event.data();

                UUID feedId = dto.feedId();
                UUID commentAuthorId = dto.author().userId();

                Feed feed = feedRepository.findById(feedId)
                    .orElseThrow(() -> new IllegalArgumentException("Feed not found: " + feedId));
                UUID feedAuthorId = feed.getAuthorId();

                if (commentAuthorId.equals(feedAuthorId)) {
                    log.debug("[NotificationRequiredTopicListener] 자기 피드 댓글, 알림 생략 - userId={}",
                        feedAuthorId);
                    return;
                }

                notificationService.create(
                    Set.of(feedAuthorId),
                    "새 댓글",
                    dto.author().name() + "님이 당신의 피드에 댓글을 남겼습니다.",
                    Level.INFO
                );

                log.debug(
                    "[NotificationRequiredTopicListener] FeedCommentCreatedEvent 처리 완료 - feedAuthorId={}, commenter={}",
                    feedAuthorId, dto.author().name());
            }
        );
    }

    @KafkaListener(
        topics = "otboo.FeedLikedEvent",
        containerFactory = "processingKafkaListenerContainerFactory"
    )
    public void onFeedLiked(String kafkaEvent, Acknowledgment ack) {
        handle(
            kafkaEvent, ack,
            "FeedLikedEvent",
            FeedLikedEvent.class,
            event -> {
                var feed = event.feed();
                var liker = event.liker();

                UUID feedAuthorId = feed.author().userId();
                String feedAuthorName = feed.author().name();
                UUID likerId = liker.id();

                if (likerId.equals(feedAuthorId)) {
                    log.debug("[NotificationRequiredTopicListener] 자기 피드 좋아요, 알림 생략 - userId={}",
                        feedAuthorId);
                    return;
                }

                notificationService.create(
                    Set.of(feedAuthorId),
                    "피드 좋아요",
                    liker.name() + "님이 당신의 피드를 좋아했습니다.",
                    Level.INFO
                );

                log.debug(
                    "[NotificationRequiredTopicListener] FeedLikedEvent 처리 완료 - feedAuthorId={}, liker={}",
                    feedAuthorName, liker.name());
            }
        );
    }


    @KafkaListener(
        topics = "otboo.FollowCreatedEvent",
        containerFactory = "processingKafkaListenerContainerFactory"
    )
    public void onFollowCreated(String kafkaEvent, Acknowledgment ack) {
        handle(
            kafkaEvent, ack,
            "FollowCreatedEvent",
            FollowCreatedEvent.class,
            event -> {
                var dto = event.data();

                UUID followerId = dto.follower().userId();
                UUID followeeId = dto.followee().userId();
                String followerName = dto.follower().name();

                notificationService.create(
                    Set.of(followeeId),
                    "새로운 팔로워",
                    followerName + "님이 당신을 팔로우했습니다.",
                    Level.INFO
                );

                log.debug(
                    "[NotificationRequiredTopicListener] FollowCreatedEvent 처리 완료 - followerId={}, followeeId={}",
                    followerId, followeeId);
            }
        );
    }

    @KafkaListener(
        topics = "otboo.RoleUpdatedEvent",
        containerFactory = "processingKafkaListenerContainerFactory"
    )
    public void onRoleUpdated(String kafkaEvent, Acknowledgment ack) {
        handle(
            kafkaEvent, ack,
            "RoleUpdatedEvent",
            RoleUpdatedEvent.class,
            event -> {
                var dto = event.data();

                UUID userId = dto.id();
                String newRole = dto.role().name();

                notificationService.create(
                    Set.of(userId),
                    "권한 변경",
                    "당신의 권한이 '" + newRole + "'로 변경되었습니다.",
                    Level.WARNING
                );

                log.debug(
                    "[NotificationRequiredTopicListener] RoleUpdatedEvent 처리 완료 - userId={}, newRole={}",
                    userId, newRole);
            }
        );
    }


    private <T> void handle(
        String kafkaEvent,
        Acknowledgment ack,
        String eventName,
        Class<T> type,
        Consumer<T> handler
    ) {
        try {
            T event = objectMapper.readValue(kafkaEvent, type);
            handler.accept(event);
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("[NotificationRequiredTopicListener] JSON 파싱 실패 ({}) - kafkaEvent: {}",
                eventName, kafkaEvent, e);
            throw new RuntimeException("Failed to parse " + eventName + " from Kafka message", e);
        } catch (Exception e) {
            log.error("[NotificationRequiredTopicListener] 처리 중 오류 발생 ({}) - kafkaEvent: {}",
                eventName, kafkaEvent, e);
            throw e;
        }
    }

    private Profile findProfile(UUID userId) {
        return profileRepository.findByUserId(userId)
            .orElseThrow(() -> new ProfileNotFoundException(userId));
    }
}
