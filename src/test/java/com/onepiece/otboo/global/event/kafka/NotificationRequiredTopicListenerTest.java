package com.onepiece.otboo.global.event.kafka;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.service.NotificationService;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.fixture.ProfileFixture;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.global.event.event.DirectMessageCreatedEvent;
import com.onepiece.otboo.global.event.event.WeatherChangeEvent;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class NotificationRequiredTopicListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private Acknowledgment ack;

    @InjectMocks
    private NotificationRequiredTopicListener listener;

    @Test
    void 날씨_변화_토픽_소비_정상_흐름_테스트() throws Exception {

        // given
        String kafkaEvent = "{\"userId\":\"6b2f0b90-6f3c-4e63-9a62-a1e6a2f7b5f4\",\"title\":\"비 예보\",\"message\":\"잠시 후 비가 옵니다.\"}";
        UUID userId = UUID.fromString("6b2f0b90-6f3c-4e63-9a62-a1e6a2f7b5f4");

        WeatherChangeEvent event = mock(WeatherChangeEvent.class);
        given(event.userId()).willReturn(userId);
        given(event.title()).willReturn("비 예보");
        given(event.message()).willReturn("잠시 후 비가 옵니다.");

        given(objectMapper.readValue(eq(kafkaEvent), eq(WeatherChangeEvent.class)))
            .willReturn(event);

        // when
        listener.onWeatherChanged(kafkaEvent, ack);

        // then
        ArgumentCaptor<Set<UUID>> setCaptor = ArgumentCaptor.forClass(Set.class);
        verify(notificationService, times(1))
            .create(setCaptor.capture(), eq("비 예보"), eq("잠시 후 비가 옵니다."), eq(Level.INFO));
        Set<UUID> targetSet = setCaptor.getValue();
        // 대상 집합에 해당 userId 포함 확인
        assert targetSet.contains(userId);
        verify(ack, times(1)).acknowledge();
        verifyNoMoreInteractions(notificationService, ack);
    }

    @Test
    void DM_생성_토픽_소비_정상_흐름_테스트() throws Exception {

        // given
        Profile senderProfile = ProfileFixture.createProfile();

        String kafkaEvent = """
            {
              "data": {
                "id": "b4c5a8de-1f6d-4d1e-8b19-3b0bcd0ff011",
                "createdAt": "2025-10-15T08:30:00Z",
                "sender": {
                  "userId": "6b2f0b90-6f3c-4e63-9a62-a1e6a2f7b5f4",
                  "name": "test",
                  "profileImageUrl": "null"
                },
                "receiver": {
                  "userId": "6b2f0b90-6f3c-4e63-9a62-a1e6a2f7b5f5",
                  "name": "han",
                  "profileImageUrl": "null"
                },
                "content": "안녕"
              },
              "createdAt": "2025-10-15T08:30:01Z"
            }
            """;
        UUID senderId = UUID.fromString("6b2f0b90-6f3c-4e63-9a62-a1e6a2f7b5f4");
        UUID receiverId = UUID.fromString("6b2f0b90-6f3c-4e63-9a62-a1e6a2f7b5f5");

        DirectMessageCreatedEvent event = mock(DirectMessageCreatedEvent.class);
        DirectMessageDto dto = mock(DirectMessageDto.class);
        AuthorDto sender = mock(AuthorDto.class);
        AuthorDto receiver = mock(AuthorDto.class);

        given(event.data()).willReturn(dto);
        given(dto.sender()).willReturn(sender);
        given(dto.receiver()).willReturn(receiver);
        given(sender.userId()).willReturn(senderId);
        given(receiver.userId()).willReturn(receiverId);
        given(dto.content()).willReturn("안녕");
        given(profileRepository.findByUserId(any(UUID.class))).willReturn(
            Optional.of(senderProfile));
        given(objectMapper.readValue(eq(kafkaEvent), eq(DirectMessageCreatedEvent.class)))
            .willReturn(event);

        // when
        listener.onDirectMessageCreated(kafkaEvent, ack);

        // then
        ArgumentCaptor<Set<UUID>> setCaptor = ArgumentCaptor.forClass(Set.class);
        verify(notificationService, times(1))
            .create(setCaptor.capture(), eq("테스트닉네임님으로부터 메시지가 왔습니다."), eq("안녕"), eq(Level.INFO));
        Set<UUID> targetSet = setCaptor.getValue();
        // 대상 집합에 해당 userId 포함 확인
        assert targetSet.contains(receiverId);
        verify(ack, times(1)).acknowledge();
        verifyNoMoreInteractions(notificationService, ack);
    }

    @Test
    void JSON_파싱_실패_테스트() throws Exception {
        // given
        String kafkaEvent = "malformed-json";
        given(objectMapper.readValue(eq(kafkaEvent), eq(WeatherChangeEvent.class)))
            .willThrow(new JsonProcessingException("boom") {
            });

        // when & then
        assertThrows(RuntimeException.class, () -> listener.onWeatherChanged(kafkaEvent, ack));
        verify(notificationService, never()).create(anySet(), anyString(), anyString(), any());
        verify(ack, never()).acknowledge();
    }

    @Test
    void 알림_생성_중_예외_발생_테스트() throws Exception {

        // given
        String kafkaEvent = "{\"ok\":true}";
        WeatherChangeEvent event = mock(WeatherChangeEvent.class);
        given(event.userId()).willReturn(UUID.randomUUID());
        given(event.title()).willReturn("알림 제목");
        given(event.message()).willReturn("알림 내용");

        given(objectMapper.readValue(eq(kafkaEvent), eq(WeatherChangeEvent.class)))
            .willReturn(event);

        doThrow(new IllegalStateException("notification error"))
            .when(notificationService)
            .create(anySet(), anyString(), anyString(), any());

        // when & then
        assertThrows(IllegalStateException.class, () -> listener.onWeatherChanged(kafkaEvent, ack));
        verify(ack, never()).acknowledge();
    }
}
