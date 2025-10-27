package com.onepiece.otboo.infra.security.config.props;

public record SwaggerProperties(
    SwaggerBasicAuthProperties basic
) {

    public SwaggerProperties {
        basic = basic == null ? SwaggerBasicAuthProperties.disabled() : basic;
    }
}