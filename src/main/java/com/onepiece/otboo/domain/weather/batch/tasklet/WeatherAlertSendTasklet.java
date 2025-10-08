package com.onepiece.otboo.domain.weather.batch.tasklet;

import com.onepiece.otboo.domain.notification.enums.AlertStatus;
import com.onepiece.otboo.domain.notification.service.NotificationService;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.weather.entity.WeatherAlertOutbox;
import com.onepiece.otboo.domain.weather.repository.WeatherAlertOutboxRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherAlertSendTasklet implements Tasklet {

    private final WeatherAlertOutboxRepository outboxRepository;
    private final ProfileRepository profileRepository;
    private final NotificationService notificationService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception {

        List<WeatherAlertOutbox> alerts = outboxRepository.findTop100ByStatus(AlertStatus.PENDING);

        if (alerts.isEmpty()) {
            log.info("[WeatherAlertSendTasklet] 보낼 알림 없음!!");
            return RepeatStatus.FINISHED;
        }

        for (WeatherAlertOutbox alert : alerts) {
            List<Profile> profiles = profileRepository.findAllByLocationId(alert.getLocationId());
            for (Profile p : profiles) {
                UUID userId = p.getUser().getId();
                notificationService.create(userId, alert.getTitle(), alert.getMessage());
            }
            alert.markSent();
        }

        outboxRepository.saveAll(alerts);
        return RepeatStatus.CONTINUABLE;
    }
}
