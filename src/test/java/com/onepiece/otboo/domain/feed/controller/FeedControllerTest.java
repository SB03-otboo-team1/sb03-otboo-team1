package com.onepiece.otboo.domain.feed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.service.FeedService;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = FeedController.class)
class FeedControllerTest {

    private static final String BASE_URL = "/api/feeds";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    FeedService feedService;

    @Test
    @WithMockUser
    void 피드_등록_정상요청시_201응답_및_위치헤더포함() throws Exception {
        UUID id = UUID.randomUUID();

        FeedResponse mocked = mock(FeedResponse.class);
        when(mocked.id()).thenReturn(id);
        when(feedService.create(any(FeedCreateRequest.class)))
            .thenReturn(mocked);

        var body = new FeedCreateRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            List.of(UUID.randomUUID()),
            "content"
        );

        mockMvc.perform(
                post("/api/feeds")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            )
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/feeds/" + id));
    }

    @Test
    @WithMockUser(username = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        // 컨트롤러가 auth.getName()을 UUID로 파싱
    void 피드_삭제_정상권한_요청시_204응답() throws Exception {
        UUID feedId = UUID.randomUUID();

        mockMvc.perform(delete(BASE_URL + "/{id}", feedId).with(csrf()))
            .andExpect(status().isNoContent());

        Mockito.verify(feedService).delete(
            eq(feedId),
            eq(UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851"))
        );
    }

    @Test
    @WithMockUser(username = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    void 피드_삭제_권한없음_요청시_403응답() throws Exception {
        UUID feedId = UUID.randomUUID();
        Mockito.doThrow(new GlobalException(ErrorCode.FEED_FORBIDDEN))
            .when(feedService).delete(eq(feedId), any(UUID.class));

        mockMvc.perform(delete(BASE_URL + "/{id}", feedId).with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
    void 피드_삭제_존재하지않는피드_요청시_404응답() throws Exception {
        UUID feedId = UUID.randomUUID();
        Mockito.doThrow(new GlobalException(ErrorCode.FEED_NOT_FOUND))
            .when(feedService).delete(eq(feedId), any(UUID.class));

        mockMvc.perform(delete(BASE_URL + "/{id}", feedId).with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    void 피드_삭제_미인증사용자_요청시_401또는302응답() throws Exception {
        UUID feedId = UUID.randomUUID();

        mockMvc.perform(delete(BASE_URL + "/{id}", feedId).with(csrf()))
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                if (status != 401 && status != 302) {
                    throw new AssertionError("인증 실패 401 또는 302 Redirect를 예상하지만 실제 응답: " + status);
                }
            });
    }
}