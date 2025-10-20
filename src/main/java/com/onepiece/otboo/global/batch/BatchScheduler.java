package com.onepiece.otboo.global.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job collectWeatherJob;

    //@Scheduled(cron = "0 15 0,6,12,18 * * *", zone = "Asia/Seoul")
    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    public void runCollectWeatherJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            log.info("[BatchScheduler] 날씨 데이터 배치 실행");

            jobLauncher.run(collectWeatherJob, params);
        } catch (Exception e) {
            log.warn("[BatchScheduler] 날씨 데이터 배치 실패: {}", e.getMessage());
        }
    }
}
