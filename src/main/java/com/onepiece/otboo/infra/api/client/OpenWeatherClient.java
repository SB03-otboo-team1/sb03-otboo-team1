package com.onepiece.otboo.infra.api.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenWeatherClient {

    private final WebClient openWeatherApiClient;
    private final ObjectMapper objectMapper;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HHmm");

    //    @Cacheable(
//        cacheNames = "owm:forecast",
//        key = "T(java.lang.String).format('%f:%f', #latitude, #longitude)",
//        sync = true
//    )
    public Root get5Day3HourForecast(double latitude, double longitude) {
        try {
            String json = openWeatherApiClient.get()
                .uri(u -> u.path("/data/2.5/forecast")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                    res.bodyToMono(String.class).flatMap(b ->
                        Mono.error(
                            new RuntimeException("OWM HTTP " + res.statusCode() + " body=" + b))
                    )
                )
                .bodyToMono(String.class)
                .block();

            Root root = objectMapper.readValue(json, Root.class);
            if (root == null || root.list == null) {
                log.warn("[OpenWeatherClient] empty/invalid JSON latitude: {}, longitude: {}",
                    latitude, longitude);
                return null;
            }
            return root;
        } catch (Exception e) {
            log.error("[OpenWeatherClient] call error latitude: {}, longitude: {}",
                latitude, longitude, e);
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Root {

        public List<Item> list;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        public long dt;
        public Main main;
        public List<W> weather;
        public Wind wind;
        public Double pop;
        public Map<String, Double> rain;
        public Map<String, Double> snow;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {

        public Double temp;
        public Double temp_min;
        public Double temp_max;
        public Double humidity;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class W {

        public int id;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Wind {

        public Double speed;      // m/s
    }

    public static String[] fcstDateTimeKST(long unixSeconds) {
        ZonedDateTime zdt = Instant.ofEpochSecond(unixSeconds).atZone(KST);
        return new String[]{zdt.format(DATE), zdt.format(TIME)};
    }
}
