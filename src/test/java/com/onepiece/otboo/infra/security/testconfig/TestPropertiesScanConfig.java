package com.onepiece.otboo.infra.security.testconfig;

import com.onepiece.otboo.infra.security.config.props.SecurityProperties;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.TestPropertySource;

@TestConfiguration
@TestPropertySource(locations = "classpath:application-test-security.yaml")
@EnableConfigurationProperties(SecurityProperties.class)
@ImportAutoConfiguration(ConfigurationPropertiesAutoConfiguration.class)
public class TestPropertiesScanConfig {

}