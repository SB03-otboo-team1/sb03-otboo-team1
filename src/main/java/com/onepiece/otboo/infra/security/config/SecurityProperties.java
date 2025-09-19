package com.onepiece.otboo.infra.security.config;

import com.onepiece.otboo.infra.security.config.props.CsrfProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otboo.security")
public record SecurityProperties(
    CsrfProperties csrf
) {

}