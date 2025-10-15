package com.onepiece.otboo.global.config;

import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.global.interceptor.JwtAuthenticationChannelInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtAuthenticationChannelInterceptor jwtAuthenticationChannelInterceptor;

    /**
     * 메시지 브로커 설정 (메모리 기반) STOMP 프로토콜에서 사용할 메시지 라우팅 규칙 정의
     *
     * @param config 메시지 브로커 설정 객체
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        log.debug("[WebSocketConfig] 메시지 브로커 설정 시작");

        config.enableSimpleBroker("/sub");

        config.setApplicationDestinationPrefixes("/pub");

        log.debug("[WebSocketConfig] 메시지 브로커 설정 완료");
    }

    /**
     * WebSocket 엔드포인트를 등록한다 클라이언트가 WebSocket 서버에 연결할 때 사용할 경로 정의
     *
     * @param registry STOMP 엔드포인트 등록 객체
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        log.debug("[WebSocketConfig] STOMP 엔드포인트 등록 시작");

        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
            .setHeartbeatTime(25000)
            .setDisconnectDelay(5000);

        log.debug("[WebSocketConfig] STOMP 엔드포인트 등록 완료");
    }

    /**
     * 클라이언트 인바운드 채널 설정 클라이언트에서 서버로 들어오는 메시지 처리를 위한 스레드 풀을 설정하고 전용 인터셉터 등록
     *
     * @param registration 채널 등록 객체
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        log.debug("[WebSocketConfig] 클라이언트 인바운드 채널 설정 시작");

        registration.interceptors(
            jwtAuthenticationChannelInterceptor,
            new SecurityContextChannelInterceptor(),
            authorizationChannelInterceptor()
        );

        log.debug("[WebSocketConfig] 클라이언트 인바운드 채널 설정 완료");
    }

    /**
     * 클라이언트 아웃바운드 채널 설정 서버에서 클라이언트로 들어오는 메시지 처리를 위한 스레드 풀을 설정
     *
     * @param registration 채널 등록 객체
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {

        log.debug("[WebSocketConfig] 클라이언트 아웃바운드 채널 설정 시작");

        registration.interceptors(jwtAuthenticationChannelInterceptor);

        log.debug("[WebSocketConfig] 클라이언트 아웃바운드 채널 설정 완료");
    }

    /**
     * 메시지 매칭 기반 인가 정책 정의
     * <p>
     * CONNECT, HEARTBEAT, UNSUBSCRIBE, DISCONNECT 등은 허용(permitAll) 발행(/pub/**) 및 구독(/sub/**)은
     * ROLE_USER 필요 - 그 외는 명시적으로 거부
     */
    private AuthorizationChannelInterceptor authorizationChannelInterceptor() {
        AuthorizationManager<Message<?>> manager =
            MessageMatcherDelegatingAuthorizationManager.builder()
                .simpTypeMatchers(
                    SimpMessageType.CONNECT,
                    SimpMessageType.HEARTBEAT,
                    SimpMessageType.DISCONNECT,
                    SimpMessageType.UNSUBSCRIBE
                ).permitAll()

                .simpDestMatchers("/pub/**").hasRole(Role.USER.name())
                .simpSubscribeDestMatchers("/sub/**").hasRole(Role.USER.name())

                .anyMessage().denyAll()
                .build();

        return new AuthorizationChannelInterceptor(manager);
    }
}
