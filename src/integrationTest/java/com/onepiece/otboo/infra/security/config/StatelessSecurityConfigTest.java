package com.onepiece.otboo.infra.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.global.dto.response.ErrorResponse;
import com.onepiece.otboo.global.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-security")
@TestPropertySource(locations = "classpath:application-test-security.yaml")
class StatelessSecurityConfigTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SIGN_IN_URI = "/api/auth/sign-in";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilterChainProxy filterChainProxy;

    @Test
    void 로그인_실패시_JSESSIONID_쿠키를_발급하지_않는다() throws Exception {
        MvcResult result = mockMvc.perform(post(SIGN_IN_URI)
                .param("email", "unknown@example.com")
                .param("password", "invalid"))
            .andExpect(status().isUnauthorized())
            .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();
        assertThat(Arrays.stream(cookies).map(Cookie::getName))
            .doesNotContain("JSESSIONID");

        ErrorResponse response = OBJECT_MAPPER.readValue(
            result.getResponse().getContentAsString(),
            ErrorResponse.class
        );
        assertThat(response.exceptionName()).isEqualTo("SecurityUnauthorizedException");
        assertThat(response.message()).isEqualTo(ErrorCode.UNAUTHORIZED.getMessage());
    }

    @Test
    void 사용자_이름_비밀번호_필터는_세션을_생성하지_않는다() {
        UsernamePasswordAuthenticationFilter filter = filterChainProxy.getFilters(SIGN_IN_URI)
            .stream()
            .filter(UsernamePasswordAuthenticationFilter.class::isInstance)
            .map(UsernamePasswordAuthenticationFilter.class::cast)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("로그인 필터를 찾을 수 없습니다."));

        Object allowSessionCreation = ReflectionTestUtils.getField(filter, "allowSessionCreation");

        assertThat(allowSessionCreation).isInstanceOf(Boolean.class);
        assertThat((Boolean) allowSessionCreation).isFalse();
    }
}