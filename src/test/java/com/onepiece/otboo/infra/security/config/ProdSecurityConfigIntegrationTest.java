package com.onepiece.otboo.infra.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("prod")
class ProdSecurityConfigIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void prod_프로필에서_보안_설정이_로드된다() {
        assertThat(applicationContext.getBean(ProdSecurityConfig.class)).isNotNull();
    }

    @Test
    void dev_전용_설정은_로드되지_않는다() {
        assertThatThrownBy(() -> applicationContext.getBean(DevSecurityConfig.class))
            .isInstanceOf(NoSuchBeanDefinitionException.class);
    }

    @Test
    void 보안_필터_체인_빈이_등록된다() {
        assertThat(applicationContext.getBean(SecurityFilterChain.class)).isNotNull();
    }
}
