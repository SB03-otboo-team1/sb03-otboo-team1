package com.onepiece.otboo.global.event.kafka;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import com.onepiece.otboo.domain.dm.fixture.DirectMessageDtoFixture;
import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.global.event.event.DirectMessageCreatedEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class WebSocketRequiredTopicListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketRequiredTopicListener listener;

    @Test
    void 발신자_수신자_사전식_비교_그대로_연결하여_메시지_구독_테스트() throws Exception {
        // given
        String kafkaEvent = "{mocked-json-not-used-because-of-stubbing}";
        UUID senderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID receiverId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        AuthorDto sender = mock(AuthorDto.class);
        AuthorDto receiver = mock(AuthorDto.class);
        given(sender.userId()).willReturn(senderId);
        given(receiver.userId()).willReturn(receiverId);

        DirectMessageDto dto = DirectMessageDtoFixture.createDirectMessageDto(sender, receiver,
            "안녕");

        DirectMessageCreatedEvent event =
            new DirectMessageCreatedEvent(dto, Instant.parse("2025-10-15T08:30:01Z"));

        given(objectMapper.readValue(kafkaEvent, DirectMessageCreatedEvent.class))
            .willReturn(event);

        // when
        listener.receiveDirectMessage(kafkaEvent);

        // then
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<DirectMessageDto> payloadCaptor = ArgumentCaptor.forClass(
            DirectMessageDto.class);
        verify(messagingTemplate, times(1))
            .convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());
        String expected = "/sub/direct-messages_" + senderId + "_" + receiverId;
        assertEquals(expected, destinationCaptor.getValue());
        assertEquals(dto, payloadCaptor.getValue());
    }

    @Test
    void 발신자_수신자_사전식_비교_사전순으로_교체하여_메시지_구독_테스트() throws Exception {
        // given
        String kafkaEvent = "{mocked-json-not-used-because-of-stubbing}";
        UUID senderId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        UUID receiverId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        AuthorDto sender = mock(AuthorDto.class);
        AuthorDto receiver = mock(AuthorDto.class);
        given(sender.userId()).willReturn(senderId);
        given(receiver.userId()).willReturn(receiverId);

        DirectMessageDto dto = DirectMessageDtoFixture.createDirectMessageDto(sender, receiver,
            "안녕");

        DirectMessageCreatedEvent event =
            new DirectMessageCreatedEvent(dto, Instant.parse("2025-10-15T08:30:01Z"));

        given(objectMapper.readValue(kafkaEvent, DirectMessageCreatedEvent.class))
            .willReturn(event);

        // when
        listener.receiveDirectMessage(kafkaEvent);

        // then
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<DirectMessageDto> payloadCaptor = ArgumentCaptor.forClass(
            DirectMessageDto.class);
        verify(messagingTemplate, times(1))
            .convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());
        String expected = "/sub/direct-messages_" + receiverId + "_" + senderId;
        assertEquals(expected, destinationCaptor.getValue());
        assertEquals(dto, payloadCaptor.getValue());
    }

    @Test
    void 역직렬화_실패시_RuntimeException_발생() throws Exception {
        // given
        String kafkaEvent = "{bad-json}";
        given(objectMapper.readValue(kafkaEvent, DirectMessageCreatedEvent.class))
            .willThrow(new JsonProcessingException("boom!") {
            });

        // when
        Throwable thrown = catchThrowable(() -> listener.receiveDirectMessage(kafkaEvent));

        // then
        assertThat(thrown)
            .isInstanceOf(RuntimeException.class);
        verifyNoInteractions(messagingTemplate);
    }
}
