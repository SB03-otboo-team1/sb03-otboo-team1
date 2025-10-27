package com.onepiece.otboo.infra.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ActiveProfiles("dev")
@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void 허용_Origin에_대해_CORS_응답_헤더가_포함된다() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.options("/api/auth/login")
                .header("Origin", "http://localhost:8080")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header()
                .string("Access-Control-Allow-Origin", "http://localhost:8080"))
            .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Methods",
                org.hamcrest.Matchers.containsString("POST")))
            .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Headers",
                org.hamcrest.Matchers.containsString("Authorization")));
    }

    @Test
    void 비허용_Origin에_대해_CORS_응답_헤더가_없다() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.options("/api/auth/login")
                .header("Origin", "http://malicious.com")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}
