package com.onepiece.otboo.domain.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiece.otboo.domain.notification.dto.response.NotificationResponse;
import com.onepiece.otboo.domain.notification.enums.Level;
import com.onepiece.otboo.domain.notification.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(NotificationController.class)
@Import(NotificationControllerTest.TestConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationService notificationService;

    @Test
    @DisplayName("알림 목록 조회 성공")
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

        when(notificationService.getNotifications(any(UUID.class)))
            .thenReturn(List.of(response));

        mockMvc.perform(get("/api/notifications/{receiverId}", receiverId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].receiverId").value(receiverId.toString()))
            .andExpect(jsonPath("$[0].title").value("새 메시지가 도착했습니다"))
            .andExpect(jsonPath("$[0].level").value("INFO"));
    }

    static class TestConfig {

        @Bean
        public NotificationService notificationService() {
            return mock(NotificationService.class);
        }
    }
}