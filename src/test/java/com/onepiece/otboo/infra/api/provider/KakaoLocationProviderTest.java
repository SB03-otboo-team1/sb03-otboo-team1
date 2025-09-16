package com.onepiece.otboo.infra.api.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.location.exception.InvalidAreaException;
import com.onepiece.otboo.domain.location.exception.InvalidCoordinateException;
import com.onepiece.otboo.infra.api.dto.KakaoLocationItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@DisplayName("카카오 API를 통한 행정구역명 반환 단위 테스트")
class KakaoLocationProviderTest {

    private WebClient webClientWith(ExchangeFunction exchange) {
        return WebClient.builder().exchangeFunction(exchange).build();
    }

    @Test
    void API_응답을_KakaoLocationItem으로_반환_테스트() {
        // given
        String okJson = """
            {
              "documents": [
                {"region_type":"H","region_1depth_name":"서울특별시","region_2depth_name":"중구","region_3depth_name":"명동","region_4depth_name":""},
                {"region_type":"B","region_1depth_name":"서울특별시","region_2depth_name":"중구","region_3depth_name":"명동","region_4depth_name":"00-0"}
              ],
              "meta": {}
            }
            """;

        ExchangeFunction exchange = req -> {
            assertThat(req.url().getPath()).endsWith("/v2/local/geo/coord2regioncode.json");
            assertThat(req.url().getQuery()).contains("x=127.0").contains("y=37.5");
            return Mono.just(ClientResponse
                .create(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(okJson)
                .build());
        };

        KakaoLocationProvider provider =
            new KakaoLocationProvider(webClientWith(exchange), new ObjectMapper());

        // when
        KakaoLocationItem item = provider.getLocation(127.0, 37.5);

        // then
        assertThat(item.region1()).isEqualTo("서울특별시");
        assertThat(item.region2()).isEqualTo("중구");
        assertThat(item.region3()).isEqualTo("명동");
        assertThat(item.region4()).isEqualTo("00-0");
    }

    @Test
    void 유효하지_않은_범위의_위도_경도로_요청을_하면_InvalidCoordinateException이_발생한다() {
        // given
        KakaoLocationProvider provider =
            new KakaoLocationProvider(webClientWith(req -> Mono.just(
                ClientResponse.create(HttpStatus.OK).body("{}").build()
            )), new ObjectMapper());

        // when
        Throwable thrown = catchThrowable(() -> provider.getLocation(200, 95));

        // then
        assertThat(thrown)
            .isInstanceOf(InvalidCoordinateException.class);
    }

    @Test
    void 서비스_지역이_아닌_곳에_대한_요청은_InvalidAreaException이_발생한다() {
        // given
        ExchangeFunction exchange = req -> Mono.just(
            ClientResponse.create(HttpStatus.BAD_REQUEST)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body("{\"message\":\"bad request\"}")
                .build()
        );

        KakaoLocationProvider provider =
            new KakaoLocationProvider(webClientWith(exchange), new ObjectMapper());

        // when
        Throwable thrown = catchThrowable(() -> provider.getLocation(140, 40));

        // then
        assertThat(thrown)
            .isInstanceOf(InvalidAreaException.class);
    }

    @Test
    void region_type_B_문서가_없으면_IllegalStateException이_발생한다() {
        // given
        String onlyHJson = """
            {
              "documents": [
                {"region_type":"H","region_1depth_name":"서울특별시","region_2depth_name":"중구","region_3depth_name":"명동","region_4depth_name":""}
              ],
              "meta": {}
            }
            """;

        ExchangeFunction exchange = req -> Mono.just(
            ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(onlyHJson)
                .build()
        );

        KakaoLocationProvider provider =
            new KakaoLocationProvider(webClientWith(exchange), new ObjectMapper());

        // when
        Throwable thrown = catchThrowable(() -> provider.getLocation(127.02, 37.51));

        // then
        assertThat(thrown)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("법정동(region_type=B) 정보를 찾을 수 없음");
    }

    @Test
    void JSON_파싱_오류_시_RuntimeException이_발생한다() {
        // given
        // 잘못된 JSON
        String badJson = "{ not-json ";

        ExchangeFunction exchange = req -> Mono.just(
            ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(badJson)
                .build()
        );

        KakaoLocationProvider provider =
            new KakaoLocationProvider(webClientWith(exchange), new ObjectMapper());

        // when
        Throwable thrown = catchThrowable(() -> provider.getLocation(126.99, 37.55));

        // then
        assertThat(thrown)
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("통신 실패");
    }
}
