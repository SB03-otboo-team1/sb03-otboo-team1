package com.onepiece.otboo.domain.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.service.NotificationService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @DisplayName("알림 목록 조회 성공 (커서 기반)")
    void getNotifications_Success() throws Exception {
        UUID receiverId = UUID.randomUUID();

        NotificationResponse response = NotificationResponse.builder()
            .id(UUID.randomUUID())
            .receiverId(receiverId)
            .title("새 메시지가 도착했습니다")
            .content("홍길동님이 메시지를 보냈습니다.")
            .level(Level.INFO)
            .createdAt(Instant.now())
            .build();

        CursorPageResponseDto<NotificationResponse> mockResponse =
            new CursorPageResponseDto<>(
                List.of(response),
                null,
                null,
                false,
                1L,
                SortBy.CREATED_AT,
                SortDirection.DESCENDING
            );

        given(notificationService.getNotifications(eq(receiverId), any(), eq(10)))
            .willReturn(mockResponse);

        mockMvc.perform(get("/api/notifications")
                .param("receiverId", receiverId.toString())
                .param("limit", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].title").value("새 메시지가 도착했습니다"))
            .andExpect(jsonPath("$.data[0].content").value("홍길동님이 메시지를 보냈습니다."))
            .andExpect(jsonPath("$.data[0].level").value("INFO"))
            .andExpect(jsonPath("$.hasNext").value(false));
    }
}