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
import static org.hamcrest.Matchers.containsString;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

        FeedResponse mocked = org.mockito.Mockito.mock(FeedResponse.class);
        org.mockito.Mockito.when(mocked.id()).thenReturn(id);
        org.mockito.Mockito.when(feedService.create(org.mockito.ArgumentMatchers.any(FeedCreateRequest.class)))
            .thenReturn(mocked);

        var body = new FeedCreateRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            java.util.List.of(UUID.randomUUID()),
            "content"
        );

        mockMvc.perform(
                post("/api/feeds")
                    .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            )
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/feeds/" + id));
    }

    @Test
    @WithMockUser
    void createFeed_returns400_whenWeatherIdNull() throws Exception {
        var body = new FeedCreateRequest(
            UUID.randomUUID(),
            null,
            java.util.List.of(UUID.randomUUID()),
            "content"
        );

        mockMvc.perform(
                post("/api/feeds")
                    .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.exceptionName").value("MethodArgumentNotValidException"))
            .andExpect(jsonPath("$.details.validationError", containsString("weatherId")))
            .andExpect(jsonPath("$.details.validationError", containsString("must not be null")));
    }
}

