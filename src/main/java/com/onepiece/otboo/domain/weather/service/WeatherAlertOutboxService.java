package com.onepiece.otboo.domain.weather.service;

import com.onepiece.otboo.domain.notification.enums.AlertStatus;
import com.onepiece.otboo.domain.weather.repository.WeatherAlertOutboxRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeatherAlertOutboxService {

    private final WeatherAlertOutboxRepository outboxRepository;

    @Transactional
    public void updateStatus(UUID id, AlertStatus status) {
        outboxRepository.findById(id).ifPresent(o -> o.updateStatus(status));
    }
}
