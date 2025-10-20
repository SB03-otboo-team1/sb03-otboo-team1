package com.onepiece.otboo.global.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.global.event.event.DirectMessageCreatedEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketRequiredTopicListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
        topics = "otboo.DirectMessageCreatedEvent",
        containerFactory = "subscriptionKafkaListenerContainerFactory"
    )
    public void receiveDirectMessage(String kafkaEvent) {
        try {
            DirectMessageCreatedEvent event = objectMapper.readValue(kafkaEvent,
                DirectMessageCreatedEvent.class);

            UUID senderId = event.data().sender().userId();
            UUID receiverId = event.data().receiver().userId();

            String destination = buildSubPath(senderId, receiverId);
            messagingTemplate.convertAndSend(destination, event.data());
        } catch (JsonProcessingException e) {
            log.warn("[WebSocketRequiredTopicListener] 변환 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String buildSubPath(UUID id1, UUID id2) {
        String s = id1.toString();
        String t = id2.toString();

        return "/sub/direct-messages_" + (s.compareTo(t) <= 0 ? s + "_" + t : t + "_" + s);
    }
}
