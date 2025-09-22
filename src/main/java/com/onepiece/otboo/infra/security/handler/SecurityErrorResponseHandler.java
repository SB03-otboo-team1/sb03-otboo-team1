package com.onepiece.otboo.infra.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.global.dto.response.ErrorResponse;
import com.onepiece.otboo.global.exception.GlobalException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponseHandler {

    private final ObjectMapper objectMapper;

    public void handle(HttpServletResponse response, GlobalException exception) throws IOException {
        response.setStatus(exception.getErrorCode().getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> details = toStringMap(exception.getDetails());
        ErrorResponse errorResponse = ErrorResponse.of(exception.getErrorCode(), exception,
            details);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private Map<String, String> toStringMap(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return Map.of();
        }
        Map<String, String> converted = new LinkedHashMap<>();
        details.forEach(
            (key, value) -> converted.put(key, value == null ? "" : String.valueOf(value)));
        return converted;
    }
}