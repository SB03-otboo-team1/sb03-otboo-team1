package com.onepiece.otboo.domain.weather.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.onepiece.otboo.domain.notification.enums.AlertStatus;
import com.onepiece.otboo.domain.weather.entity.WeatherAlertOutbox;
import com.onepiece.otboo.domain.weather.repository.WeatherAlertOutboxRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeatherAlertOutboxServiceTest {

    @Mock
    private WeatherAlertOutboxRepository outboxRepository;

    @InjectMocks
    private WeatherAlertOutboxService outboxService;

    @Test
    void 데이터가_있는_경우_상태_변경_테스트() {

        // given
        UUID id = UUID.randomUUID();

        WeatherAlertOutbox outbox = mock(WeatherAlertOutbox.class);

        given(outboxRepository.findById(id)).willReturn(Optional.of(outbox));

        // when
        outboxService.updateStatus(id, AlertStatus.SEND);

        // then
        verify(outbox).updateStatus(AlertStatus.SEND);
        verify(outboxRepository).findById(eq(id));
    }

    @Test
    void 데이터가_없는_경우_상태_변경_테스트() {

        // given
        UUID id = UUID.randomUUID();

        given(outboxRepository.findById(id)).willReturn(Optional.empty());

        // when
        outboxService.updateStatus(id, AlertStatus.SEND);

        // then
        verify(outboxRepository).findById(eq(id));
        verifyNoMoreInteractions(outboxRepository);
    }
}