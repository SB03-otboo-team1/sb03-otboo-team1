package com.onepiece.otboo.infra.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class DevSecurityConfigIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dev_프로필에서_보안_설정이_로드된다() {
        assertThat(applicationContext.getBean(DevSecurityConfig.class)).isNotNull();
    }

    @Test
    void prod_전용_설정은_로드되지_않는다() {
        assertThatThrownBy(() -> applicationContext.getBean(ProdSecurityConfig.class))
            .isInstanceOf(NoSuchBeanDefinitionException.class);
    }

    @Test
    void 보안_필터_체인_빈이_등록된다() {
        Map<String, SecurityFilterChain> chains = applicationContext.getBeansOfType(
            SecurityFilterChain.class);
        assertThat(chains).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void dev_프로필에서는_h2_console에_접근할_수_있다() throws Exception {
        MvcResult result = mockMvc.perform(get("/h2-console/"))
            .andReturn();

        assertThat(result.getResponse().getStatus())
            .isNotIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value());
    }
}
