package com.onepiece.otboo.infra.api.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.location.exception.InvalidAreaException;
import com.onepiece.otboo.domain.location.exception.InvalidCoordinateException;
import com.onepiece.otboo.infra.api.dto.KakaoLocationItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoLocationProvider implements LocationProvider {

    private final WebClient kakaoWebClient;
    private final ObjectMapper objectMapper;

    @Override
    public KakaoLocationItem getLocation(double longitude, double latitude) {
        log.info("[LocationProvider] 위치 정보 조회 요청 - longitude: {}, latitude: {}",
            longitude, latitude);

        if (!validLatLon(longitude, latitude)) {
            throw new InvalidCoordinateException();
        }

        return parseItemFromApi(longitude, latitude);
    }

    private boolean validLatLon(double longitude, double latitude) {
        return longitude >= -180 && longitude <= 180 && latitude >= -90 && latitude <= 90;
    }

    private KakaoLocationItem parseItemFromApi(Double longitude, Double latitude) {
        try {
            String response = kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v2/local/geo/coord2regioncode.json")
                    .queryParam("x", longitude)
                    .queryParam("y", latitude)
                    .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, res ->
                    res.bodyToMono(String.class).map(err ->
                        new InvalidAreaException(latitude, longitude)))
                .bodyToMono(String.class)
                .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode docs = root.path("documents");

            // documents 배열에서 region_type = "B"인 항목만 선택
            for (JsonNode doc : docs) {
                if ("B".equals(doc.path("region_type").asText())) {
                    return new KakaoLocationItem(
                        doc.path("region_1depth_name").asText(),
                        doc.path("region_2depth_name").asText(),
                        doc.path("region_3depth_name").asText(),
                        doc.path("region_4depth_name").asText()
                    );
                }
            }

            throw new IllegalStateException("법정동(region_type=B) 정보를 찾을 수 없음 - 위도, 경도: " +
                longitude + "," + latitude);
        } catch (Exception e) {
            log.error("[KakaoLocationProvider] Kakao API 통신 중 오류 발생 - message: {}", e.getMessage());
            throw new RuntimeException("Kakao API 통신 실패", e);
        }
    }
}
