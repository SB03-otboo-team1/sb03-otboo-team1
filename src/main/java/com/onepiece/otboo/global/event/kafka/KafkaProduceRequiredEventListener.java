package com.onepiece.otboo.global.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.global.event.event.WeatherChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProduceRequiredEventListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC_PREFIX = "otboo.";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(WeatherChangeEvent event) {
        send(event);
    }

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
}
