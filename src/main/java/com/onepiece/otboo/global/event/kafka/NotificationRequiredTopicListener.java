package com.onepiece.otboo.global.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.service.NotificationService;
import com.onepiece.otboo.global.event.event.WeatherChangeEvent;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredTopicListener {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(
        topics = "otboo.WeatherChangeEvent",
        containerFactory = "processingKafkaListenerContainerFactory"
    )
    public void onWeatherChanged(String kafkaEvent) {
        try {
            WeatherChangeEvent event = objectMapper.readValue(kafkaEvent, WeatherChangeEvent.class);
            UUID userId = event.userId();
            String title = event.title();
            String content = event.message();

            notificationService.create(Set.of(userId), title, content, Level.INFO);

            log.debug("[NotificationRequiredTopicListener] 날씨 변화 알림 생성 완료 - userId: {}",
                userId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
