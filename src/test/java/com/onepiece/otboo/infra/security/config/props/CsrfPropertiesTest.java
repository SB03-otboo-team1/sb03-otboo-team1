package com.onepiece.otboo.infra.security.config.props;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

class CsrfPropertiesTest {

    @Test
    void 무시_경로_설정이_없으면_빈_매처_배열을_반환한다() {
        RequestMatcher[] matchers = new CsrfProperties(null).ignoredRequestMatchers();
        assertThat(matchers).isEmpty();
    }

    @Test
    void 등록한_패턴과_일치하면_매처가_참을_반환한다() {
        CsrfProperties properties = new CsrfProperties(List.of("/api/**", "/actuator/**"));

        RequestMatcher[] matchers = properties.ignoredRequestMatchers();
        assertThat(matchers).hasSize(2);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users/1");
        assertThat(matchesAny(matchers, request)).isTrue();

        request.setRequestURI("/static/logo.svg");
        assertThat(matchesAny(matchers, request)).isFalse();
    }

    @Test
    void 컨텍스트_경로는_요청_URI_비교에서_제외한다() {
        CsrfProperties properties = new CsrfProperties(List.of("/api/**"));
        RequestMatcher[] matchers = properties.ignoredRequestMatchers();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/app");
        request.setRequestURI("/app/api/auth/csrf-token");

        assertThat(matchesAny(matchers, request)).isTrue();
    }

    private boolean matchesAny(RequestMatcher[] matchers, HttpServletRequest request) {
        for (RequestMatcher matcher : matchers) {
            if (matcher.matches(request)) {
                return true;
            }
        }
        return false;
    }
}