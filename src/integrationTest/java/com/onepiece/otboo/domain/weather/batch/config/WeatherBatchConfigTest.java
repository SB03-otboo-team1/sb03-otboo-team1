package com.onepiece.otboo.domain.weather.batch.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.fixture.LocationFixture;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import com.onepiece.otboo.infra.api.client.OpenWeatherClient;
import com.onepiece.otboo.infra.api.client.OpenWeatherClient.Main;
import com.onepiece.otboo.infra.api.client.OpenWeatherClient.Root;
import com.onepiece.otboo.infra.api.client.OpenWeatherClient.W;
import com.onepiece.otboo.infra.api.client.OpenWeatherClient.Wind;
import java.time.Instant;
import java.util.ArrayList;
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
    private OpenWeatherClient openWeatherClient;

    private UUID savedLocationId;

    @BeforeEach
    void setUp() {
        weatherRepository.deleteAll();

        locationRepository.deleteAll();

        jobLauncherTestUtils.setJob(collectWeatherJob);

        Location saved = locationRepository.save(LocationFixture.createLocation());
        savedLocationId = saved.getId();

        Root root = buildOpenWeatherStub();

        given(openWeatherClient.get5Day3HourForecast(anyDouble(), anyDouble()))
            .willReturn(root);
    }

    private Root buildOpenWeatherStub() {
        Root root = new Root();
        root.list = new ArrayList<>();

        // 3시간 간격 5개 정도 샘플 아이템
        long now = Instant.now().getEpochSecond();

        for (int i = 0; i < 5; i++) {
            OpenWeatherClient.Item item = new OpenWeatherClient.Item();
            item.dt = now + i * 3 * 3600;

            Main main = new Main();
            main.temp = 293.15 + i;   // K (대충 값)
            main.temp_min = 292.15 + i;
            main.temp_max = 294.15 + i;
            main.humidity = 60.0;
            item.main = main;

            W w = new W();
            w.id = 500; // 비 코드 등
            item.weather = List.of(w);

            Wind wind = new Wind();
            wind.speed = 3.2;
            item.wind = wind;

            item.pop = 0.2;

            root.list.add(item);
        }

        return root;
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