package com.onepiece.otboo.global.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.notification.enums.AlertStatus;
import com.onepiece.otboo.domain.weather.service.WeatherAlertOutboxService;
import com.onepiece.otboo.global.event.event.DirectMessageCreatedEvent;
import com.onepiece.otboo.global.event.event.WeatherChangeEvent;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProduceRequiredEventListener {

    private static final String TOPIC_PREFIX = "otboo.";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final WeatherAlertOutboxService outboxService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(WeatherChangeEvent event) {
        send(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(DirectMessageCreatedEvent event) {
        send(event);
    }

    // 일반적인 이벤트 처리
    private void send(Object event) {
        try {
            String topic = TOPIC_PREFIX + event.getClass().getSimpleName();
            String payload = objectMapper.writeValueAsString(event);

            log.debug("[KafkaProduceRequiredEventListener] 이벤트 발행 - topic: {} payload: {}",
                topic, payload);

            kafkaTemplate.send(topic, payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // 날씨 변화 이벤트 처리
    private void send(WeatherChangeEvent event) {
        final String topic = TOPIC_PREFIX + WeatherChangeEvent.class.getSimpleName();
        final String payload;

        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            outboxService.updateStatus(event.outboxId(), AlertStatus.FAILED);
            log.error("[KafkaProducer] 직렬화 오류 - outboxId={}, topic={}", event.outboxId(), topic, e);
            return;
        }

        log.debug("[KafkaProducer] 발행 요청 - topic: {}, payload: {}", topic, payload);

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, payload);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                outboxService.updateStatus(event.outboxId(), AlertStatus.SEND);
                log.info("[Kafka] 발행 성공 - outboxId={}, topic={}, offset={}",
                    event.outboxId(), topic, result.getRecordMetadata().offset());
            } else {
                outboxService.updateStatus(event.outboxId(), AlertStatus.FAILED);
                log.error("[Kafka] 발행 실패 - outboxId={}, topic={}, cause={}",
                    event.outboxId(), topic, ex.getMessage(), ex);
            }
        });
    }
}
