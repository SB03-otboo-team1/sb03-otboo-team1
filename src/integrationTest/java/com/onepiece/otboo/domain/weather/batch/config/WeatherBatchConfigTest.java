package com.onepiece.otboo.domain.weather.batch.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.fixture.LocationFixture;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.fixture.KmaItemFixture;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import com.onepiece.otboo.infra.api.client.KmaClient;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test-integration")
@TestPropertySource(properties = "spring.cache.type=NONE")
@SpringBatchTest
@SpringBootTest
class WeatherBatchConfigTest {

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:weatherbatchtestdb");
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job collectWeatherJob;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private WeatherRepository weatherRepository;

    @MockitoBean
    private KmaClient kmaClient;

    private UUID savedLocationId;

    @BeforeEach
    void setUp() {
        weatherRepository.deleteAll();

        locationRepository.deleteAll();

        jobLauncherTestUtils.setJob(collectWeatherJob);

        Location saved = locationRepository.save(LocationFixture.createLocation());
        savedLocationId = saved.getId();

        given(kmaClient.getVillageForecast(anyInt(), anyInt(), any(LocalDate.class), anyString()))
            .willAnswer(inv -> {
                int nx = inv.getArgument(0, Integer.class);
                int ny = inv.getArgument(1, Integer.class);
                LocalDate baseDate = inv.getArgument(2, LocalDate.class);
                String baseTime = inv.getArgument(3, String.class);
                return KmaItemFixture.buildVillageItemsForFiveDays(nx, ny, baseDate, baseTime);
            });
    }

    @Test
    void 날씨_수집_Job_성공_테스트() throws Exception {
        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        List<Weather> weathers = weatherRepository.findAll();
        assertThat(weathers).isNotEmpty();
        assertEquals(savedLocationId, weathers.get(0).getLocation().getId());
        assertNotNull(weathers.get(0).getTemperatureCurrent());
        assertNotNull(weathers.get(0).getForecastAt());
    }

    @Test
    void 날씨_수집_Step_성공_테스트() {
        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("collectWeatherStep");

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        List<Weather> weathers = weatherRepository.findAll();
        assertThat(weathers).hasSizeGreaterThan(0);
    }

    @AfterEach
    void tearDown() {
        weatherRepository.deleteAll();
        locationRepository.deleteAll();
    }
}