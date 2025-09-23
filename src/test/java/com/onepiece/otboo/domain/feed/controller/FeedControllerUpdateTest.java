package com.onepiece.otboo.domain.feed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.service.FeedService;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeedController.class)
class FeedControllerUpdateTest {

    private static final String BASE_URL = "/api/feeds";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    FeedService feedService;

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void 피드_수정_성공시_200응답_username_UUID() throws Exception {
        UUID feedId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID requesterId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        FeedResponse mocked = Mockito.mock(FeedResponse.class);
        when(feedService.update(eq(feedId), eq(requesterId), any(FeedUpdateRequest.class)))
            .thenReturn(mocked);

        var body = new FeedUpdateRequest("updated content");
        mockMvc.perform(
            patch(BASE_URL + "/{id}", feedId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isOk());

        verify(feedService, times(1)).update(eq(feedId), eq(requesterId), any(FeedUpdateRequest.class));
        verifyNoMoreInteractions(feedService);
    }

    @Test
    void 피드_수정_성공시_200응답_principal_CustomUserDetails() throws Exception {
        UUID feedId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID requesterId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        CustomUserDetails cud = Mockito.mock(CustomUserDetails.class);
        when(cud.getUserId()).thenReturn(requesterId);
        var token = new TestingAuthenticationToken(cud, null);
        token.setAuthenticated(true);

        FeedResponse mocked = Mockito.mock(FeedResponse.class);
        when(feedService.update(eq(feedId), eq(requesterId), any(FeedUpdateRequest.class)))
            .thenReturn(mocked);

        var body = new FeedUpdateRequest("server principal path");
        mockMvc.perform(
            patch(BASE_URL + "/{id}", feedId)
                .with(csrf())
                .with(authentication(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isOk());

        verify(feedService, times(1)).update(eq(feedId), eq(requesterId), any(FeedUpdateRequest.class));
        verifyNoMoreInteractions(feedService);
    }

    @Test
    @WithMockUser(username = "not-a-uuid")
    void 피드_수정_잘못된_Principal_UUID_형식시_403응답() throws Exception {
        UUID feedId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

        var body = new FeedUpdateRequest("content");
        mockMvc.perform(
            patch(BASE_URL + "/{id}", feedId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(feedService);
    }

    @Test
    @WithMockUser(username = "33333333-3333-3333-3333-333333333333")
    void 피드_수정_유효성검증_실패시_400응답() throws Exception {
        UUID feedId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

        String tooLong = "x".repeat(1001);
        var body = new FeedUpdateRequest(tooLong);

        mockMvc.perform(
            patch(BASE_URL + "/{id}", feedId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isBadRequest());

        verifyNoInteractions(feedService);
    }

    @Test
    @WithMockUser(username = "44444444-4444-4444-4444-444444444444")
    void 피드_수정_FEED_NOT_FOUND_시_404응답() throws Exception {
        UUID feedId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
        UUID requesterId = UUID.fromString("44444444-4444-4444-4444-444444444444");

        doThrow(new GlobalException(ErrorCode.FEED_NOT_FOUND))
            .when(feedService).update(eq(feedId), eq(requesterId), any(FeedUpdateRequest.class));

        var body = new FeedUpdateRequest("content");
        mockMvc.perform(
            patch(BASE_URL + "/{id}", feedId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "55555555-5555-5555-5555-555555555555")
    void 피드_수정_FEED_FORBIDDEN_시_403응답() throws Exception {
        UUID feedId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        UUID requesterId = UUID.fromString("55555555-5555-5555-5555-555555555555");

        doThrow(new GlobalException(ErrorCode.FEED_FORBIDDEN))
            .when(feedService).update(eq(feedId), eq(requesterId), any(FeedUpdateRequest.class));

        var body = new FeedUpdateRequest("content");
        mockMvc.perform(
            patch(BASE_URL + "/{id}", feedId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isForbidden());
    }

    @Test
    void 피드_수정_미인증사용자_요청시_401응답() throws Exception {
        UUID feedId = UUID.randomUUID();
        var body = new FeedUpdateRequest("content");

        mockMvc.perform(
            patch(BASE_URL + "/{id}", feedId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isUnauthorized());
    }
}
