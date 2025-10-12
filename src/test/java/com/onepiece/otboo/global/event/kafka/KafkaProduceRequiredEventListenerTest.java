package com.onepiece.otboo.global.event.kafka;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.notification.enums.AlertStatus;
import com.onepiece.otboo.domain.weather.service.WeatherAlertOutboxService;
import com.onepiece.otboo.global.event.event.WeatherChangeEvent;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class KafkaProduceRequiredEventListenerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private WeatherAlertOutboxService outboxService;

    @InjectMocks
    private KafkaProduceRequiredEventListener listener;

    private static final String TOPIC = "otboo.WeatherChangeEvent";

    @Test
    void 직렬화_성공및_카프카_전송_성공_테스트() throws Exception {

        // given
        UUID outboxId = UUID.randomUUID();
        WeatherChangeEvent event = mock(WeatherChangeEvent.class);
        given(event.outboxId()).willReturn(outboxId);

        String payload = "{\"foo\":\"bar\"}";
        given(objectMapper.writeValueAsString(event)).willReturn(payload);

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        given(kafkaTemplate.send(TOPIC, payload)).willReturn(future);

        // when
        listener.on(event); // 내부에서 send(WeatherChangeEvent) 호출

        // then(성공 콜백 트리거)
        SendResult sendResult = mock(SendResult.class);
        RecordMetadata recordMetadata = mock(RecordMetadata.class);
        given(sendResult.getRecordMetadata()).willReturn(recordMetadata);
        given(recordMetadata.offset()).willReturn(42L);
        future.complete(sendResult);
        verify(kafkaTemplate, times(1)).send(TOPIC, payload);
        verify(outboxService, times(1)).updateStatus(outboxId, AlertStatus.SEND);
        verify(outboxService, never()).updateStatus(outboxId, AlertStatus.FAILED);
    }

    @Test
    void 직렬화_실패_테스트() throws Exception {

        // given
        UUID outboxId = UUID.randomUUID();
        WeatherChangeEvent event = mock(WeatherChangeEvent.class);
        given(event.outboxId()).willReturn(outboxId);

        given(objectMapper.writeValueAsString(event))
            .willThrow(new JsonProcessingException("boom") {
            });

        // when
        listener.on(event);

        // then
        verify(outboxService, times(1)).updateStatus(outboxId, AlertStatus.FAILED);
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void 카프카_전송_실패_테스트() throws Exception {

        // given
        UUID outboxId = UUID.randomUUID();
        WeatherChangeEvent event = mock(WeatherChangeEvent.class);
        given(event.outboxId()).willReturn(outboxId);

        String payload = "{\"ok\":true}";
        given(objectMapper.writeValueAsString(event)).willReturn(payload);

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        given(kafkaTemplate.send(TOPIC, payload)).willReturn(future);

        // when
        listener.on(event);

        // then
        future.completeExceptionally(new RuntimeException("send failed"));
        verify(kafkaTemplate, times(1)).send(TOPIC, payload);
        verify(outboxService, times(1)).updateStatus(outboxId, AlertStatus.FAILED);
        verify(outboxService, never()).updateStatus(outboxId, AlertStatus.SEND);
    }
}
