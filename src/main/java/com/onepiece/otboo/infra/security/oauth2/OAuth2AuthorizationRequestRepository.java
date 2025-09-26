package com.onepiece.otboo.infra.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * OAuth2 인증 요청 저장소 인터페이스 세션/쿠키/외부 저장소 등 추후 확장
 */
public interface OAuth2AuthorizationRequestRepository {

    void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request, HttpServletResponse response);

    OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request);

    void removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response);
}
