package com.onepiece.otboo.infra.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class ProdSecurityConfigIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

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
        Map<String, SecurityFilterChain> chains = applicationContext.getBeansOfType(
            SecurityFilterChain.class);
        assertThat(chains).isNotEmpty();
    }

    @Test
    void prod_프로필에서는_h2_console_접근이_차단된다() throws Exception {
        mockMvc.perform(get("/h2-console"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void prod_프로필에서_swagger_인증_실패시_401() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isUnauthorized());
    }
}
