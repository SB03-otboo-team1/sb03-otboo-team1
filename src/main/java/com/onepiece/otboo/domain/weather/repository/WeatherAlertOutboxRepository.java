package com.onepiece.otboo.domain.weather.repository;

import com.onepiece.otboo.domain.notification.enums.AlertStatus;
import com.onepiece.otboo.domain.weather.entity.WeatherAlertOutbox;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherAlertOutboxRepository extends JpaRepository<WeatherAlertOutbox, UUID> {

    List<WeatherAlertOutbox> findTop100ByStatus(AlertStatus status);
}
