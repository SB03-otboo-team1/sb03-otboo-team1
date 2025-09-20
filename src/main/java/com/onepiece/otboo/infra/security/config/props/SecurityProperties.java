package com.onepiece.otboo.infra.security.config.props;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otboo.security")
public record SecurityProperties(
    CsrfProperties csrf,
    SwaggerProperties swagger
) {

    public SecurityProperties {
        csrf = csrf == null ? new CsrfProperties(List.of()) : csrf;
        swagger = swagger == null ? new SwaggerProperties(null) : swagger;
    }
}
