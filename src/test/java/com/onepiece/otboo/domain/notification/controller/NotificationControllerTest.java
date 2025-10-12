package com.onepiece.otboo.domain.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiece.otboo.domain.notification.Controller.NotificationController;
import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("알림 목록 조회 성공")
    void getNotifications_Success() throws Exception {
        UUID receiverId = UUID.randomUUID();

        NotificationResponse notificationResponse = NotificationResponse.builder()
            .id(UUID.randomUUID())
            .receiverId(receiverId)
            .title("새 팔로워 알림")
            .content("민준님을 팔로우했습니다.")
            .level("INFO")
            .createdAt(Instant.now())
            .isRead(false)
            .readAt(null)
            .build();

        CursorPageResponseDto<NotificationResponse> mockResponse =
            new CursorPageResponseDto<>(
                List.of(notificationResponse),
                "cursor123",
                UUID.randomUUID(),
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
            .andExpect(jsonPath("$.data[0].title").value("새 팔로워 알림"))
            .andExpect(jsonPath("$.data[0].content").value("민준님을 팔로우했습니다."))
            .andExpect(jsonPath("$.data[0].level").value("INFO"))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("알림 읽음 처리 성공 (PATCH /api/notifications/{id}/read)")
    void markAsRead_Success() throws Exception {
        UUID notificationId = UUID.randomUUID();
        doNothing().when(notificationService).markAsRead(notificationId);

        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }
}