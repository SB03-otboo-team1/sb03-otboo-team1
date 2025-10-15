package com.onepiece.otboo.domain.dm.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import com.onepiece.otboo.domain.dm.service.DirectMessageService;
import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DirectMessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class DirectMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DirectMessageService directMessageService;

    @Test
    @DisplayName("DM 목록 조회 성공")
    void getDirectMessages_success() throws Exception {
        UUID userId = UUID.randomUUID();

        AuthorDto sender = AuthorDto.builder()
            .userId(userId)
            .name("sender")
            .profileImageUrl("sender.png")
            .build();

        AuthorDto receiver = AuthorDto.builder()
            .userId(UUID.randomUUID())
            .name("receiver")
            .profileImageUrl("receiver.png")
            .build();

        DirectMessageDto mockResponse = DirectMessageDto.builder()
            .id(UUID.randomUUID())
            .createdAt(Instant.now())
            .sender(sender)
            .receiver(receiver)
            .content("조회 테스트")
            .build();

        given(directMessageService.getDirectMessages(eq(userId), any(), any(), anyInt()))
            .willReturn(new CursorPageResponseDto<>(List.of(mockResponse), null, null, false, 5L,
                SortBy.CREATED_AT, SortDirection.DESCENDING));

        mockMvc.perform(get("/api/direct-messages")
                .param("userId", userId.toString())
                .param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].content").value("조회 테스트"));
    }
}