package com.onepiece.otboo.infra.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.global.dto.response.ErrorResponse;
import com.onepiece.otboo.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-security")
@TestPropertySource(locations = "classpath:application-test-security.yaml")
@Import(SecurityExceptionHandlingTest.ProtectedTestController.class)
class SecurityExceptionHandlingTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 인증되지_않은_요청은_JSON_401을_반환한다() throws Exception {
        String content = mockMvc.perform(get("/internal/protected"))
            .andExpect(status().isUnauthorized())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ErrorResponse response = OBJECT_MAPPER.readValue(content, ErrorResponse.class);
        assertErrorResponse(response, ErrorCode.UNAUTHORIZED);
    }

    @Test
    void 권한이_부족하면_JSON_403을_반환한다() throws Exception {
        String content = mockMvc.perform(
                get("/internal/admin-only").with(user("tester").roles("USER")))
            .andExpect(status().isForbidden())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ErrorResponse response = OBJECT_MAPPER.readValue(content, ErrorResponse.class);
        assertErrorResponse(response, ErrorCode.FORBIDDEN);
    }

    private void assertErrorResponse(ErrorResponse response, ErrorCode errorCode) {
        assertThat(response.exceptionName()).isNotBlank();
        assertThat(response.message()).isEqualTo(errorCode.getMessage());
    }

    @RestController
    static class ProtectedTestController {

        @GetMapping("/internal/protected")
        public String protectedEndpoint() {
            return "secured";
        }

        @GetMapping("/internal/admin-only")
        @PreAuthorize("hasRole('ADMIN')")
        public String adminEndpoint() {
            return "admin";
        }
    }
}