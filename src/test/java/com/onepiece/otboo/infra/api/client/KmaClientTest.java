package com.onepiece.otboo.infra.api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.infra.api.dto.KmaItem;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
@DisplayName("KMA 단기예보 API 호출 단위 테스트")
class KmaClientTest {

    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private WebClient webClientWith(ExchangeFunction exchange) {
        return WebClient.builder().exchangeFunction(exchange).build();
    }

    @Test
    void 성공_JSON이면_아이템목록을_반환한다() {
        // given
        String okJson = """
            {
              "response": {
                "header": { "resultCode": "00", "resultMsg": "NORMAL_SERVICE" },
                "body": {
                  "items": {
                    "item": [
                      { },
                      { }
                    ]
                  }
                }
              }
            }
            """;

        ExchangeFunction exchange = req -> {
            assertThat(req.url().getPath())
                .endsWith("/api/typ02/openApi/VilageFcstInfoService_2.0/getVilageFcst");
            assertThat(req.url().getQuery()).contains("numOfRows=1000");
            assertThat(req.url().getQuery()).contains("pageNo=1");
            assertThat(req.url().getQuery()).contains("nx=60");
            assertThat(req.url().getQuery()).contains("ny=127");
            assertThat(req.url().getQuery()).contains("base_time=0200");
            assertThat(req.url().getQuery()).contains("base_date=20250929");

            return Mono.just(ClientResponse.create(HttpStatusCode.valueOf(200))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(okJson)
                .build());
        };

        KmaClient client = new KmaClient(webClientWith(exchange), new ObjectMapper());

        // when
        List<KmaItem> result = client.getVillageForecast(60, 127, LocalDate.of(2025, 9, 29),
            "0200");

        // then
        assertEquals(2, result.size());
    }

    @Test
    void resultCode가_00이_아니면_빈리스트를_반환한다() {
        // given
        String notOkJson = """
            {
              "response": {
                "header": { "resultCode": "03", "resultMsg": "INVALID_REQUEST" },
                "body": { "items": { "item": [ { } ] } }
              }
            }
            """;

        ExchangeFunction exchange = req -> Mono.just(
            ClientResponse.create(HttpStatusCode.valueOf(200))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(notOkJson)
                .build()
        );

        KmaClient client = new KmaClient(webClientWith(exchange), new ObjectMapper());

        // when
        List<KmaItem> list = client.getVillageForecast(60, 127, LocalDate.of(2025, 9, 29), "0200");

        // then
        assertThat(list).isEmpty();
    }

    @Test
    void HTTP오류가_나면_빈리스트를_반환한다() {
        // given: onStatus(HttpStatusCode::isError, ...)에 걸리도록 500 응답
        ExchangeFunction exchange = req -> Mono.just(
            ClientResponse.create(HttpStatusCode.valueOf(500))
                .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                .body("server error")
                .build()
        );

        KmaClient client = new KmaClient(webClientWith(exchange), new ObjectMapper());

        // when
        List<KmaItem> list = client.getVillageForecast(60, 127, LocalDate.of(2025, 9, 29), "0200");

        // then: 내부에서 예외를 잡고 빈 리스트 반환
        assertThat(list).isEmpty();
    }

    @Test
    void 빈바디나_스키마누락_JSON이면_빈리스트를_반환한다() {
        // given: response/header 없음
        String emptyJson = "{}";

        ExchangeFunction exchange = req -> Mono.just(
            ClientResponse.create(HttpStatusCode.valueOf(200))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(emptyJson)
                .build()
        );

        KmaClient client = new KmaClient(webClientWith(exchange), new ObjectMapper());

        // when
        List<KmaItem> list = client.getVillageForecast(60, 127, LocalDate.of(2025, 9, 29), "0200");

        // then
        assertThat(list).isEmpty();
    }

    @Test
    void items가_null이면_빈리스트를_반환한다() {
        // given: body는 있으나 items가 없음
        String noItemsJson = """
            {
              "response": {
                "header": { "resultCode": "00", "resultMsg": "NORMAL_SERVICE" },
                "body": { }
              }
            }
            """;

        ExchangeFunction exchange = req -> Mono.just(
            ClientResponse.create(HttpStatusCode.valueOf(200))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(noItemsJson)
                .build()
        );

        KmaClient client = new KmaClient(webClientWith(exchange), new ObjectMapper());

        // when
        List<KmaItem> list = client.getVillageForecast(60, 127, LocalDate.of(2025, 9, 29), "0200");

        // then
        assertThat(list).isEmpty();
    }

    @Test
    void 잘못된JSON이면_빈리스트를_반환한다() {
        // given: 파싱 불가 JSON → catch 후 빈 리스트
        String badJson = "{ not-json ";

        ExchangeFunction exchange = req -> Mono.just(
            ClientResponse.create(HttpStatusCode.valueOf(200))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(badJson)
                .build()
        );

        KmaClient client = new KmaClient(webClientWith(exchange), new ObjectMapper());

        // when
        List<KmaItem> list = client.getVillageForecast(60, 127, LocalDate.of(2025, 9, 29), "0200");

        // then
        assertThat(list).isEmpty();
    }
}
