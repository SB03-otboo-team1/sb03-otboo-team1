package com.onepiece.otboo.infra.security.config.props;

import java.util.List;

public record CorsProperties(
    List<String> allowedOrigins,
    List<String> allowedMethods,
    List<String> allowedHeaders,
    Boolean allowCredentials
) {

    public CorsProperties {
        allowCredentials = allowCredentials == null ? Boolean.TRUE : allowCredentials;
    }
}
