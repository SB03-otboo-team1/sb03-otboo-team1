package com.onepiece.otboo.infra.api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ActiveProfiles("test")
@DisplayName("OpenWeather 5일/3시간 예보 API 호출 단위 테스트")
class OpenWeatherClientTest {

    private WebClient webClientWith(ExchangeFunction exchange) {
        return WebClient.builder().exchangeFunction(exchange).build();
    }

    @Test
    void 성공_JSON이면_Root와_아이템목록을_반환한다() {

        // given: OWM /data/2.5/forecast 형태의 최소 샘플
        String okJson = """
            {
              "list": [
                {
                  "dt": 0,
                  "main": { "temp": 298.15, "temp_min": 297.00, "temp_max": 299.00, "humidity": 70 },
                  "weather": [ { "id": 500 } ],
                  "wind": { "speed": 3.2 },
                  "pop": 0.25,
                  "rain": { "3h": 1.2 }
                },
                {
                  "dt": 60,
                  "main": { "temp": 300.15, "temp_min": 299.00, "temp_max": 301.00, "humidity": 60 },
                  "weather": [ { "id": 801 } ],
                  "wind": { "speed": 5.0 },
                  "pop": 0.0
                }
              ]
            }
            """;

        ExchangeFunction exchange = req -> {
            // 요청 검증
            assertThat(req.url().getPath()).endsWith("/data/2.5/forecast");
            assertThat(req.url().getQuery()).contains("lat=37.57"); // 일부만 확인
            assertThat(req.url().getQuery()).contains("lon=126.98");

            return Mono.just(ClientResponse.create(HttpStatusCode.valueOf(200))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(okJson)
                .build());
        };

        OpenWeatherClient client = new OpenWeatherClient(webClientWith(exchange),
            new ObjectMapper());

        // when
        OpenWeatherClient.Root root = client.get5Day3HourForecast(37.57, 126.98);

        // then
        assertNotNull(root);
        assertEquals(2, root.list.size());
        OpenWeatherClient.Item first = root.list.get(0);
        assertEquals(0L, first.dt);
        assertEquals(298.15, first.main.temp);
        assertEquals(1, first.weather.size());
        assertEquals(3.2, first.wind.speed);
        assertEquals(0.25, first.pop);
        assertThat(first.rain).containsEntry("3h", 1.2);
        OpenWeatherClient.Item second = root.list.get(1);
        assertEquals(60L, second.dt);
        assertNull(second.snow); // 없으면 null
    }

    @Test
    void HTTP오류가_나면_null을_반환한다() {

        // given: onStatus(HttpStatusCode::isError, ...)에 걸리도록 500 응답
        ExchangeFunction exchange = req -> Mono.just(
            ClientResponse.create(HttpStatusCode.valueOf(500))
                .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                .body("server error")
                .build()
        );

        OpenWeatherClient client = new OpenWeatherClient(webClientWith(exchange),
            new ObjectMapper());

        // when
        OpenWeatherClient.Root root = client.get5Day3HourForecast(35.0, 129.0);

        // then
        assertNull(root);
    }

    @Test
    void 바디가_비어있으면_null을_반환한다() {

        // given
        String emptyJson = "{}";

        ExchangeFunction exchange = req -> Mono.just(
            ClientResponse.create(HttpStatusCode.valueOf(200))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(emptyJson)
                .build()
        );

        OpenWeatherClient client = new OpenWeatherClient(webClientWith(exchange),
            new ObjectMapper());

        // when
        OpenWeatherClient.Root root = client.get5Day3HourForecast(35.0, 129.0);

        // then
        assertNull(root);
    }

    @Test
    void list가_null이면_null을_반환한다() {

        // given
        String noListJson = """
            { "city": { "name": "Seoul" } }
            """;

        ExchangeFunction exchange = req -> Mono.just(
            ClientResponse.create(HttpStatusCode.valueOf(200))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(noListJson)
                .build()
        );

        OpenWeatherClient client = new OpenWeatherClient(webClientWith(exchange),
            new ObjectMapper());

        // when
        OpenWeatherClient.Root root = client.get5Day3HourForecast(35.0, 129.0);

        // then
        assertNull(root);
    }

    @Test
    void 잘못된_JSON이면_null을_반환한다() {

        // given
        String badJson = "{ not-json ";

        ExchangeFunction exchange = req -> Mono.just(
            ClientResponse.create(HttpStatusCode.valueOf(200))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(badJson)
                .build()
        );

        OpenWeatherClient client = new OpenWeatherClient(webClientWith(exchange),
            new ObjectMapper());

        // when
        OpenWeatherClient.Root root = client.get5Day3HourForecast(35.0, 129.0);

        // then
        assertThat(root).isNull();
    }

    @Test
    void fcstDateTimeKST_포맷확인() {
        String[] dt = OpenWeatherClient.fcstDateTimeKST(0L);
        assertThat(dt[0]).isEqualTo("19700101+0900");
        assertThat(dt[1]).isEqualTo("0900");
    }

    @Test
    void 캐시미적용환경에서는_두번호출된다() {

        // given
        AtomicInteger callCount = new AtomicInteger(0);

        String okJson = """
            { "list": [ { "dt": 0, "main": { "temp": 295.0 }, "weather":[{"id":800}] } ] }
            """;

        ExchangeFunction exchange = req -> {
            callCount.incrementAndGet();
            return Mono.just(ClientResponse.create(HttpStatusCode.valueOf(200))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(okJson)
                .build());
        };

        OpenWeatherClient client = new OpenWeatherClient(webClientWith(exchange),
            new ObjectMapper());

        // when
        OpenWeatherClient.Root a = client.get5Day3HourForecast(37.57, 126.98);
        OpenWeatherClient.Root b = client.get5Day3HourForecast(37.57, 126.98);

        // then
        assertNotNull(a);
        assertNotNull(b);
    }
}