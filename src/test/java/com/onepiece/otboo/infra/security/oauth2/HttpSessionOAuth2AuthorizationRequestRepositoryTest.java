package com.onepiece.otboo.infra.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@ExtendWith(MockitoExtension.class)
class HttpSessionOAuth2AuthorizationRequestRepositoryTest {

    private static final String SESSION_KEY = "OAUTH2_AUTHORIZATION_REQUEST";

    private HttpSessionOAuth2AuthorizationRequestRepository repository;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private OAuth2AuthorizationRequest authRequest;

    @BeforeEach
    void setUp() {
        repository = new HttpSessionOAuth2AuthorizationRequestRepository();
        when(request.getSession()).thenReturn(session);
    }

    @Test
    void 인증_요청_저장_시_세션에_저장된다() {
        repository.saveAuthorizationRequest(authRequest, request, response);
        verify(session).setAttribute(SESSION_KEY, authRequest);
    }

    @Test
    void null_저장_시_세션에서_삭제된다() {
        repository.saveAuthorizationRequest(null, request, response);
        verify(session).removeAttribute(SESSION_KEY);
    }

    @Test
    void 세션에서_인증_요청을_조회할_수_있다() {
        when(session.getAttribute(SESSION_KEY)).thenReturn(authRequest);
        OAuth2AuthorizationRequest loaded = repository.loadAuthorizationRequest(request);
        assertThat(loaded).isEqualTo(authRequest);
    }

    @Test
    void 세션에_없으면_null을_반환한다() {
        when(session.getAttribute(SESSION_KEY)).thenReturn(null);
        OAuth2AuthorizationRequest loaded = repository.loadAuthorizationRequest(request);
        assertThat(loaded).isNull();
    }

    @Test
    void 인증_요청_삭제_시_세션에서_삭제된다() {
        repository.removeAuthorizationRequest(request, response);
        verify(session).removeAttribute(SESSION_KEY);
    }
}
