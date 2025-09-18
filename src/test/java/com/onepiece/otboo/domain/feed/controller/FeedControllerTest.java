package com.onepiece.otboo.domain.feed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.service.FeedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeedController.class)
class FeedControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    FeedService feedService;

    @Test
    @WithMockUser
    void createFeed_returns201_withLocationHeader() throws Exception {
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
}