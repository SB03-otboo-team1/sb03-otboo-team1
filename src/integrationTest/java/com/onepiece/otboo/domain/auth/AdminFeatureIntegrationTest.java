package com.onepiece.otboo.domain.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-integration")
class AdminFeatureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${otboo.admin.email}")
    private String adminEmail;
    @Value("${otboo.admin.password}")
    private String adminPassword;

    @Test
    void 서버_시작시_어드민_계정_자동_초기화() throws Exception {
        User admin = userRepository.findByEmail(adminEmail).orElse(null);
        Assertions.assertNotNull(admin);
        Assertions.assertEquals("ADMIN", admin.getRole().name());
        Assertions.assertTrue(passwordEncoder.matches(adminPassword, admin.getPassword()));
    }

    @Test
    void 사용자_권한_변경_시_자동_로그아웃() throws Exception {
        TestUserContext user = registerAndLogin("user1@otboo.com", "user1Pw!", "테스트유저1");
        String changeRolePayload = "{\"role\":\"ADMIN\"}";
        String adminToken = getAdminToken();
        mockMvc.perform(patch("/api/users/" + user.userId + "/role")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(changeRolePayload)
                .with(csrf()))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/users/" + user.userId)
                .header("Authorization", user.token))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 계정_잠금_및_로그인_불가_자동_로그아웃() throws Exception {
        TestUserContext user = registerAndLogin("user2@otboo.com", "user2pw!", "테스트유저2");
        String lockPayload = "{\"locked\":true}";
        String adminToken = getAdminToken();
        mockMvc.perform(patch("/api/users/" + user.userId + "/lock")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(lockPayload)
                .with(csrf()))
            .andExpect(status().isOk());
        mockMvc.perform(multipart("/api/auth/sign-in")
                .param("username", "user2@otboo.com")
                .param("password", "user2pw!")
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/" + user.userId)
                .header("Authorization", user.token))
            .andExpect(status().isUnauthorized());
    }

    private TestUserContext registerAndLogin(String email, String password, String name)
        throws Exception {
        String signupPayload = String.format(
            "{\"email\":\"%s\",\"password\":\"%s\",\"name\":\"%s\"}", email, password, name);
        MvcResult signupResult = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupPayload)
                .with(csrf()))
            .andExpect(status().isCreated())
            .andReturn();
        String userId = extractUserId(signupResult);

        MvcResult loginResult = mockMvc.perform(multipart("/api/auth/sign-in")
                .param("username", email)
                .param("password", password)
                .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();
        String responseBody = loginResult.getResponse().getContentAsString();
        String token =
            "Bearer " + new ObjectMapper().readTree(responseBody).get("accessToken").asText();
        return new TestUserContext(userId, token);
    }

    private static class TestUserContext {

        final String userId;
        final String token;

        TestUserContext(String userId, String token) {
            this.userId = userId;
            this.token = token;
        }
    }

    private String extractUserId(MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();

        int idIdx = response.indexOf("\"id\":");
        if (idIdx < 0) {
            throw new IllegalStateException("userId not found in response");
        }
        int start = response.indexOf('"', idIdx + 5) + 1;
        int end = response.indexOf('"', start);

        return response.substring(start, end);
    }

    private String getAdminToken() throws Exception {
        MvcResult adminLoginResult = mockMvc.perform(multipart("/api/auth/sign-in")
                .param("username", adminEmail)
                .param("password", adminPassword)
                .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();
        String responseBody = adminLoginResult.getResponse().getContentAsString();
        return "Bearer " + new ObjectMapper().readTree(responseBody).get("accessToken").asText();
    }
}
