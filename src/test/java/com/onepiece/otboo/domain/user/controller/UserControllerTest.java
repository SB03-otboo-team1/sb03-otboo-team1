package com.onepiece.otboo.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.user.dto.request.UserCreateRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.service.UserService;
import com.onepiece.otboo.global.config.JpaConfig;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(value = UserController.class,
    excludeAutoConfiguration = {JpaConfig.class}
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController 단위 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 회원가입_성공시_201을_반환한다() throws Exception {
        // given
        UserCreateRequest userCreateRequest = new UserCreateRequest("test", "test@test.com",
            "test1234@");

        UserDto userDto = UserDto.builder()
            .id(UUID.randomUUID())
            .createdAt(Instant.now())
            .email("test@test.com")
            .name("test")
            .role(Role.USER)
            .linkedOAuthProviders(List.of(Provider.LOCAL))
            .locked(false)
            .build();

        given(userService.create(any(UserCreateRequest.class))).willReturn(userDto);

        // when
        ResultActions result = mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userCreateRequest)));

        // then
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("test"))
            .andExpect(jsonPath(("$.email")).value("test@test.com"));
        verify(userService).create(argThat(req ->
            req.name().equals("test") &&
                req.email().equals("test@test.com") &&
                req.password().equals("test1234@")));
    }

    @Test
    void 이름을_20자_넘게_입력시_400_에러를_반환한다() throws Exception {
        // given
        String longName = "a".repeat(21);

        UserCreateRequest invalidRequest = new UserCreateRequest(longName, "aa@aa.com",
            "pwd1234@");

        // when
        ResultActions result = mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 올바르지_않은_형식의_이메일로_회원가입_요청시_400_에러를_반환한다() throws Exception {
        // given
        String invalidEmail = "test#test.com";

        UserCreateRequest invalidRequest = new UserCreateRequest("test", invalidEmail,
            "test1234@");

        // when
        ResultActions result = mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 올바르지_않은_형식의_비밀번호로_회원가입_요청시_400_에러를_반환한다() throws Exception {
        // given
        List<String> invalidPasswords = List.of(
            "123456", // 영어, 숫자, 특수문자 포함 조건 위반
            "qwer111",
            "2313@@",
            "q12@" // 6자 이상 조건 위반
        );

        for (String pwd : invalidPasswords) {
            String invalidRequest = String.format("""
                    {
                      "name": "test",
                      "email": "test@test.coom",
                      "password": "%s"
                    }
                """, pwd);

            // when
            ResultActions result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest));

            // then
            result.andExpect(status().isBadRequest());
        }
    }
}