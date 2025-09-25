package com.onepiece.otboo.domain.follow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.domain.follow.service.FollowService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FollowController.class)
@AutoConfigureMockMvc(addFilters = false) // Security 무시
@DisplayName("FollowController 단위 테스트")
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FollowService followService;

    @Test
    @DisplayName("팔로우 생성 성공")
    void createFollow_success() throws Exception {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        FollowRequest request = new FollowRequest(followerId, followeeId);

        FollowResponse response = FollowResponse.builder()
            .id(UUID.randomUUID())
            .followerId(followerId)
            .nickname("팔로워닉네임")
            .profileImageUrl("follower.png")
            .createdAt(Instant.now())
            .build();

        given(followService.createFollow(any(FollowRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/follows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.followerId").value(followerId.toString()))
            .andExpect(jsonPath("$.nickname").value("팔로워닉네임"));
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공 (커서 기반)")
    void getFollowers_success() throws Exception {
        UUID userId = UUID.randomUUID();

        FollowResponse followResponse = FollowResponse.builder()
            .id(UUID.randomUUID())
            .followerId(UUID.randomUUID())
            .nickname("팔로워닉네임")
            .profileImageUrl("profile.png")
            .createdAt(Instant.now())
            .build();

        CursorPageResponseDto<FollowResponse> mockResponse =
            new CursorPageResponseDto<>(List.of(followResponse), "cursor123", UUID.randomUUID().toString(),
                false, 1L, "createdAt", "ASC");

        given(followService.getFollowers(eq(userId), any(), any(), anyInt(), any(), any(), any()))
            .willReturn(mockResponse);

        mockMvc.perform(get("/api/follows/followers/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].nickname").value("팔로워닉네임"));
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공 (커서 기반)")
    void getFollowings_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID followingId = UUID.randomUUID();

        FollowingResponse followingResponse = FollowingResponse.builder()
            .id(UUID.randomUUID())
            .followingId(followingId)
            .nickname("팔로잉닉네임")
            .profileImage("profile.png")
            .createdAt(Instant.now())
            .build();

        CursorPageResponseDto<FollowingResponse> mockResponse =
            new CursorPageResponseDto<>(List.of(followingResponse), "cursor456", UUID.randomUUID().toString(),
                false, 1L, "createdAt", "ASC");

        given(followService.getFollowings(eq(userId), any(), any(), anyInt(), any(), any(), any()))
            .willReturn(mockResponse);

        mockMvc.perform(get("/api/follows/followings/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].followingId").value(followingId.toString()))
            .andExpect(jsonPath("$.data[0].nickname").value("팔로잉닉네임"));
    }

    @Test
    @DisplayName("언팔로우 성공")
    void deleteFollow_success() throws Exception {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        FollowRequest request = new FollowRequest(followerId, followeeId);

        doNothing().when(followService).deleteFollow(any(FollowRequest.class));

        mockMvc.perform(delete("/api/follows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("팔로우 요약 조회 성공")
    void getFollowSummary_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID viewerId = UUID.randomUUID();

        FollowSummaryResponse response = FollowSummaryResponse.builder()
            .userId(userId)
            .followerCount(5)
            .followingCount(3)
            .isFollowing(true)
            .build();

        given(followService.getFollowSummary(eq(userId), eq(viewerId))).willReturn(response);

        mockMvc.perform(get("/api/follows/summary/{userId}", userId)
                .param("viewerId", viewerId.toString())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.followerCount").value(5))
            .andExpect(jsonPath("$.followingCount").value(3))
            .andExpect(jsonPath("$.isFollowing").value(true));
    }
}