package com.onepiece.otboo.domain.weather.batch.config;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import com.onepiece.otboo.domain.weather.batch.processor.Weather5DayProcessor;
import com.onepiece.otboo.domain.weather.batch.writer.WeatherDataWriter;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeatherBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final LocationRepository locationRepository;
    private final WeatherRepository weatherRepository;
    private final Weather5DayProcessor weather5DayProcessor;

    @Bean
    public Job collectWeatherJob() {
        return new JobBuilder("collectWeatherJob", jobRepository)
            .start(collectWeatherStep())
            .build();
    }

    @Bean
    public Step collectWeatherStep() {

        ItemWriter<List<Weather>> writer = new WeatherDataWriter(weatherRepository);

        return new StepBuilder("collectWeatherStep", jobRepository)
            .<Location, List<Weather>>chunk(100, transactionManager)
            .reader(locationItemReader())
            .processor(weather5DayProcessor)
            .writer(writer)
            .faultTolerant()
            .retryLimit(2)
            .retry(RuntimeException.class)
            .skipLimit(1000)
            .build();
    }

    @Bean
    @StepScope
    public ItemReader<Location> locationItemReader() {
        return new IteratorItemReader<>(locationRepository.findAll());
    }
}
