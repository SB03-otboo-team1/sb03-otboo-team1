package com.onepiece.otboo.domain.comment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.comment.dto.request.CommentCreateRequest;
import com.onepiece.otboo.domain.comment.dto.response.CommentDto;
import com.onepiece.otboo.domain.comment.service.CommentService;
import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CommentService commentService;

    @Test
    @WithMockUser
    void 댓글_등록_성공_200() throws Exception {
        // given
        UUID feedId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        CommentCreateRequest req = new CommentCreateRequest(null, authorId, "내용");

        CommentDto dto = new CommentDto(
            UUID.randomUUID(),
            Instant.now(),
            feedId,
            new AuthorDto(authorId, "", null),
            "내용"
        );
        when(commentService.create(eq(feedId), any(CommentCreateRequest.class)))
            .thenReturn(dto);

        // when & then
        mockMvc.perform(post("/api/feeds/{feedId}/comments", feedId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.feedId").value(feedId.toString()))
            .andExpect(jsonPath("$.author.userId").value(authorId.toString()))
            .andExpect(jsonPath("$.content").value("내용"));

        // 서비스 위임 파라미터 검증
        ArgumentCaptor<CommentCreateRequest> captor = ArgumentCaptor.forClass(
            CommentCreateRequest.class);
        verify(commentService, times(1)).create(eq(feedId), captor.capture());
        assertThat(captor.getValue().authorId()).isEqualTo(authorId);
        assertThat(captor.getValue().content()).isEqualTo("내용");
    }

    @Test
    @WithMockUser
    void 내용_없으면_실패_400() throws Exception {
        UUID feedId = UUID.randomUUID();
        CommentCreateRequest req = new CommentCreateRequest(null, UUID.randomUUID(), "   ");

        mockMvc.perform(post("/api/feeds/{feedId}/comments", feedId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
        verify(commentService, never()).create(any(), any());
    }

    @Test
    @WithMockUser
    void 내용_200자_초과시_실패_400() throws Exception {
        UUID feedId = UUID.randomUUID();
        String over200 = "a".repeat(201);
        CommentCreateRequest req = new CommentCreateRequest(null, UUID.randomUUID(), over200);

        mockMvc.perform(post("/api/feeds/{feedId}/comments", feedId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
        verify(commentService, never()).create(any(), any());
    }
}
