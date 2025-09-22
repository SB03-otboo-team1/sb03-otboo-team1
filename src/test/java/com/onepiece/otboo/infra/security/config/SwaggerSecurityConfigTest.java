package com.onepiece.otboo.infra.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@TestPropertySource(properties = {
    "otboo.security.swagger.basic.enabled=true",
    "otboo.security.swagger.basic.username=admin",
    "otboo.security.swagger.basic.password=adminpass"
})
class SwaggerSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 스웨거_인증_실패시_401반환() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/swagger-ui/index.html"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void 스웨거_인증_성공시_200반환() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/swagger-ui/index.html")
                .header("Authorization", "Basic YWRtaW46YWRtaW5wYXNz"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
