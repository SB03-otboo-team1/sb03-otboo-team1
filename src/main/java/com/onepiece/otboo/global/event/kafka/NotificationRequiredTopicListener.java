package com.onepiece.otboo.global.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.service.NotificationService;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.exception.ProfileNotFoundException;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.global.event.event.DirectMessageCreatedEvent;
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
