package com.onepiece.otboo.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(1)
public class HttpLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            Map<String, Object> payload = new HashMap<>();
            payload.put("requestId", MDC.get(MdcLoggingFilter.MDC_REQUEST_ID));
            payload.put("path", MDC.get(MdcLoggingFilter.MDC_PATH));
            payload.put("userId", MDC.get(MdcLoggingFilter.MDC_USER_ID));
            payload.put("status", response.getStatus());
            payload.put("elapsedMs", elapsed);
            log.info("[HttpLoggingFilter] {}", payload);
        }
    }
}

