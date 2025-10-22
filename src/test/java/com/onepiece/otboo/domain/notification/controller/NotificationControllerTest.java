package com.onepiece.otboo.domain.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.service.NotificationService;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @DisplayName("알림 목록 조회 성공 (GET /api/notifications)")
    void getNotifications_Success() throws Exception {
        UUID userId = UUID.randomUUID();
        CustomUserDetails mockUser = new CustomUserDetails(
            userId,
            "test@email.com",
            "password",
            Role.USER,
            false,
            null,
            Instant.now().plusSeconds(3600)
        );

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        NotificationResponse notificationResponse = NotificationResponse.builder()
            .id(UUID.randomUUID())
            .receiverId(userId)
            .title("새 팔로워 알림")
            .content("민준님을 팔로우했습니다.")
            .level("INFO")
            .createdAt(Instant.now())
            .build();

        CursorPageResponseDto<NotificationResponse> mockResponse =
            new CursorPageResponseDto<>(
                List.of(notificationResponse),
                "2025-10-15T09:00:00Z",
                UUID.randomUUID(),
                false,
                1L,
                SortBy.CREATED_AT,
                SortDirection.DESCENDING
            );

        given(notificationService.getNotifications(any(UUID.class), anyString(), any(), eq(10)))
            .willReturn(mockResponse);

        mockMvc.perform(get("/api/notifications")
                .param("cursor", "2025-10-14T10:15:30Z")
                .param("idAfter", UUID.randomUUID().toString())
                .param("limit", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].title").value("새 팔로워 알림"))
            .andExpect(jsonPath("$.data[0].content").value("민준님을 팔로우했습니다."))
            .andExpect(jsonPath("$.data[0].level").value("INFO"))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("알림 삭제 성공 (DELETE /api/notifications/{id})")
    void deleteNotification_Success() throws Exception {
        UUID notificationId = UUID.randomUUID();
        doNothing().when(notificationService).deleteNotification(notificationId);

        mockMvc.perform(delete("/api/notifications/{id}", notificationId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }
}