package com.onepiece.otboo.infra.security.config.props;

import java.util.List;
import org.springframework.http.server.PathContainer;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

public record CsrfProperties(
    List<String> ignoredPaths
) {

    private static final PathPatternParser PATH_PATTERN_PARSER = new PathPatternParser();

    public CsrfProperties {
        ignoredPaths = (ignoredPaths == null || ignoredPaths.isEmpty())
            ? List.of()
            : List.copyOf(ignoredPaths);
    }

    public RequestMatcher[] ignoredRequestMatchers() {
        return ignoredPaths.stream()
            .map(PATH_PATTERN_PARSER::parse)
            .map(this::toRequestMatcher)
            .toArray(RequestMatcher[]::new);
    }

    private RequestMatcher toRequestMatcher(PathPattern pathPattern) {
        return request -> {
            String contextPath = request.getContextPath();
            String requestPath = request.getRequestURI();
            if (!contextPath.isEmpty() && requestPath.startsWith(contextPath)) {
                requestPath = requestPath.substring(contextPath.length());
            }
            return pathPattern.matches(PathContainer.parsePath(requestPath));
        };
    }
}