package com.onepiece.otboo.global.util;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

public class TestUtils {

    /**
     * JSON 객체를 multipart/form-data의 JSON part로 변환
     *
     * @param objectMapper 테스트에서 주입받은 ObjectMapper
     * @param partName     part 이름 (@RequestPart 파라미터명과 동일해야 함)
     * @param value        직렬화할 객체
     * @return MockPart (Content-Type: application/json)
     */
    public static MockPart jsonPart(ObjectMapper objectMapper, String partName, Object value) {
        try {
            MockPart part = new MockPart(
                partName,
                objectMapper.writeValueAsBytes(value)
            );
            part.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return part;
        } catch (Exception e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }

    public static MockPart jsonPart(String partName, String rawJson) {
        MockPart part = new MockPart(partName, rawJson.getBytes(StandardCharsets.UTF_8));
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return part;
    }

    public static MockMultipartHttpServletRequestBuilder multipartPatch(String urlTemplate, Object... uriVars) {
        return multipart(HttpMethod.PATCH, urlTemplate, uriVars);
    }
}
