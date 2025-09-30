package com.onepiece.otboo.domain.dm.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.dm.dto.request.DirectMessageRequest;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageResponse;
import com.onepiece.otboo.domain.dm.service.DirectMessageService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DirectMessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class DirectMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DirectMessageService directMessageService;

    @Test
    @DisplayName("DM 생성 성공")
    void createDirectMessage_success() throws Exception {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        DirectMessageRequest request = new DirectMessageRequest(senderId, receiverId, "테스트 메시지");

        DirectMessageResponse mockResponse = DirectMessageResponse.builder()
            .id(UUID.randomUUID())
            .createdAt(Instant.now())
            .sender(new DirectMessageResponse.UserInfo(senderId, "sender@test.com"))
            .receiver(new DirectMessageResponse.UserInfo(receiverId, "receiver@test.com"))
            .content("테스트 메시지")
            .build();

        org.mockito.Mockito.when(directMessageService.createDirectMessage(any()))
            .thenReturn(mockResponse);

        mockMvc.perform(post("/api/direct-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("테스트 메시지"));
    }

    @Test
    @DisplayName("DM 목록 조회 성공")
    void getDirectMessages_success() throws Exception {
        UUID userId = UUID.randomUUID();

        DirectMessageResponse mockResponse = DirectMessageResponse.builder()
            .id(UUID.randomUUID())
            .createdAt(Instant.now())
            .sender(new DirectMessageResponse.UserInfo(UUID.randomUUID(), "sender@test.com"))
            .receiver(new DirectMessageResponse.UserInfo(userId, "receiver@test.com"))
            .content("조회 테스트")
            .build();

        org.mockito.Mockito.when(
                directMessageService.getDirectMessages(eq(userId), any(), any(), anyInt()))
            .thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/direct-messages")
                .param("userId", userId.toString())
                .param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].content").value("조회 테스트"));
    }
}