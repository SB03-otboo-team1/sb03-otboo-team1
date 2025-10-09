package com.onepiece.otboo.domain.weather.batch.tasklet;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.onepiece.otboo.domain.notification.enums.AlertStatus;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.fixture.ProfileFixture;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import com.onepiece.otboo.domain.weather.entity.WeatherAlertOutbox;
import com.onepiece.otboo.domain.weather.repository.WeatherAlertOutboxRepository;
import com.onepiece.otboo.global.event.event.WeatherChangeEvent;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WeatherAlertSendTaskletTest {

    @Mock
    private WeatherAlertOutboxRepository outboxRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private WeatherAlertSendTasklet tasklet;

    @Test
    void 대기_중인_알림이_없으면_FINISHED를_반환한다() throws Exception {

        // given
        given(outboxRepository.findTop100ByStatus(AlertStatus.PENDING)).willReturn(List.of());

        // when
        RepeatStatus status = tasklet.execute(mock(StepContribution.class),
            mock(ChunkContext.class));

        // then
        assertEquals(RepeatStatus.FINISHED, status);
        verify(outboxRepository).findTop100ByStatus(AlertStatus.PENDING);
        verifyNoMoreInteractions(outboxRepository, profileRepository, publisher);
    }

    @Test
    void 사용자에게_보낼_알림_생성_후_SENDING_상태로_변경() throws Exception {

        // given
        UUID locationId1 = UUID.randomUUID();
        UUID locationId2 = UUID.randomUUID();

        WeatherAlertOutbox outbox1 = WeatherAlertOutbox.builder()
            .locationId(locationId1)
            .title("강풍 주의")
            .message("강풍이 예보되어 있습니다.")
            .status(AlertStatus.PENDING)
            .build();

        WeatherAlertOutbox outbox2 = WeatherAlertOutbox.builder()
            .locationId(locationId2)
            .title("폭우 주의")
            .message("폭우가 예보되어 있습니다.")
            .status(AlertStatus.PENDING)
            .build();

        given(outboxRepository.findTop100ByStatus(AlertStatus.PENDING)).willReturn(
            List.of(outbox1, outbox2));

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        User u1 = UserFixture.createUser("test1@test.com");
        User u2 = UserFixture.createUser("test2@test.com");
        User u3 = UserFixture.createUser("test3@test.com");
        ReflectionTestUtils.setField(u1, "id", id1);
        ReflectionTestUtils.setField(u2, "id", id2);
        ReflectionTestUtils.setField(u3, "id", id3);

        Profile p1 = ProfileFixture.createProfile(u1);
        Profile p2 = ProfileFixture.createProfile(u2);
        Profile p3 = ProfileFixture.createProfile(u3);

        given(profileRepository.findAllByLocationId(locationId1)).willReturn(List.of(p1, p2));
        given(profileRepository.findAllByLocationId(locationId2)).willReturn(List.of(p3));

        // when
        RepeatStatus status = tasklet.execute(mock(StepContribution.class),
            mock(ChunkContext.class));

        // then
        assertEquals(RepeatStatus.CONTINUABLE, status);
        ArgumentCaptor<List<WeatherAlertOutbox>> captor = ArgumentCaptor.forClass(List.class);
        verify(outboxRepository).saveAll(captor.capture());
        List<WeatherAlertOutbox> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved).allMatch(x -> x.getStatus() == AlertStatus.SENDING);
        verify(outboxRepository).findTop100ByStatus(AlertStatus.PENDING);
        verify(profileRepository).findAllByLocationId(locationId1);
        verify(profileRepository).findAllByLocationId(locationId2);
        ArgumentCaptor<WeatherChangeEvent> evtCap = ArgumentCaptor.forClass(
            WeatherChangeEvent.class);
        verify(publisher, times(3)).publishEvent(any(WeatherChangeEvent.class));
    }
}