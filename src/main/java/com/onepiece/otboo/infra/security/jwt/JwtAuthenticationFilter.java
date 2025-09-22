package com.onepiece.otboo.infra.security.jwt;

import com.onepiece.otboo.global.logging.MdcLoggingFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtProvider.validateAccessToken(token)) {
                var authentication = jwtProvider.getAuthentication(token);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.debug(
                        "JWT 인증 객체 생성 실패 - requestId={}, path={}",
                        currentRequestId(),
                        request.getRequestURI()
                    );
                    SecurityContextHolder.clearContext();
                }
            } else {
                log.debug(
                    "JWT 토큰 검증 실패 - requestId={}, path={}",
                    currentRequestId(),
                    request.getRequestURI()
                );
                SecurityContextHolder.clearContext();
            }
        } catch (Exception ex) {
            log.warn(
                "JWT 처리 중 예외 발생 - requestId={}, path={}, message={}",
                currentRequestId(),
                request.getRequestURI(),
                ex.getMessage(),
                ex
            );
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(BEARER_PREFIX.length()).trim();
            return StringUtils.hasText(token) ? token : null;
        }
        return null;
    }

    private String currentRequestId() {
        return Optional.ofNullable(MDC.get(MdcLoggingFilter.MDC_REQUEST_ID)).orElse("-");
    }
}