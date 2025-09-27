package com.onepiece.otboo.infra.security.oauth2.handler;

import com.onepiece.otboo.infra.security.exception.SecurityUnauthorizedException;
import com.onepiece.otboo.infra.security.handler.SecurityErrorResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    private final SecurityErrorResponseHandler responseHandler;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException exception) throws IOException {
        responseHandler.handle(response, new SecurityUnauthorizedException(exception));
    }
}
