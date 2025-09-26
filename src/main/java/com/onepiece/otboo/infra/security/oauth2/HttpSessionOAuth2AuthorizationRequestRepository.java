package com.onepiece.otboo.infra.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class HttpSessionOAuth2AuthorizationRequestRepository implements
    OAuth2AuthorizationRequestRepository {

    private static final String SESSION_ATTR_NAME = "OAUTH2_AUTHORIZATION_REQUEST";

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            request.getSession().removeAttribute(SESSION_ATTR_NAME);
        } else {
            request.getSession().setAttribute(SESSION_ATTR_NAME, authorizationRequest);
        }
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Object attr = request.getSession().getAttribute(SESSION_ATTR_NAME);
        if (attr instanceof OAuth2AuthorizationRequest oAuth2AuthorizationRequest) {
            return oAuth2AuthorizationRequest;
        }
        return null;
    }

    @Override
    public void removeAuthorizationRequest(HttpServletRequest request,
        HttpServletResponse response) {
        request.getSession().removeAttribute(SESSION_ATTR_NAME);
    }
}
