package com.onepiece.otboo.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient kakaoApiClient(@Value("${api.kakao.rest-api-key}") String key) {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000) // TCP 연결 타임아웃 4초
            .responseTimeout(Duration.ofSeconds(5))  // 서버 응답 타임아웃 5초
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS))
            );

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl("https://dapi.kakao.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + key)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
