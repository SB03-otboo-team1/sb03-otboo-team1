package com.onepiece.otboo.infra.security.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.onepiece.otboo.global.config.PropertiesScanConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SecurityPropertiesBindTest {

    private static final String SWAGGER_PREFIX = "otboo.security.swagger.basic";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
        .withUserConfiguration(PropertiesScanConfig.class);

    @Test
    void bindsDefaultSwaggerProperties() {
        contextRunner.run(context -> {
            SecurityProperties properties = context.getBean(SecurityProperties.class);

            assertThat(properties.csrf()).isNotNull();
            assertThat(properties.csrf().ignoredPaths()).isEmpty();

            assertThat(properties.swagger()).isNotNull();
            assertThat(properties.swagger().basic().enabled()).isFalse();
            assertThat(properties.swagger().basic().username()).isNull();
            assertThat(properties.swagger().basic().password()).isNull();
        });
    }

    @Test
    void bindsSwaggerPropertiesFromEnvironment() {
        contextRunner
            .withPropertyValues(
                "otboo.security.csrf.ignored-paths[0]=/api/auth/**",
                SWAGGER_PREFIX + ".enabled=true",
                SWAGGER_PREFIX + ".username=swagger-admin",
                SWAGGER_PREFIX + ".password=swagger-pass"
            )
            .run(context -> {
                SecurityProperties properties = context.getBean(SecurityProperties.class);

                assertThat(properties.csrf().ignoredPaths())
                    .containsExactly("/api/auth/**");

                assertThat(properties.swagger().basic().enabled()).isTrue();
                assertThat(properties.swagger().basic().username()).isEqualTo("swagger-admin");
                assertThat(properties.swagger().basic().password()).isEqualTo("swagger-pass");
            });
    }
}
