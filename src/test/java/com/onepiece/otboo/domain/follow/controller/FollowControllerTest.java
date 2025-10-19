package com.onepiece.otboo.domain.follow.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowDto;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryDto;
import com.onepiece.otboo.domain.follow.exception.FollowNotFoundException;
import com.onepiece.otboo.domain.follow.service.FollowService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.exception.ErrorCode;
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

@WebMvcTest(FollowController.class)
@AutoConfigureMockMvc(addFilters = false)
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

        AuthorDto follower = AuthorDto.builder()
            .userId(followerId)
            .name("팔로워닉네임")
            .profileImageUrl("follower.png")
            .build();

        AuthorDto followee = AuthorDto.builder()
            .userId(followeeId)
            .name("팔로이닉네임")
            .profileImageUrl("followee.png")
            .build();

        FollowDto response = FollowDto.builder()
            .id(UUID.randomUUID())
            .follower(follower)
            .followee(followee)
            .build();

        given(followService.createFollow(any(FollowRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/follows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.follower.userId").value(followerId.toString()))
            .andExpect(jsonPath("$.follower.name").value("팔로워닉네임"))
            .andExpect(jsonPath("$.followee.userId").value(followeeId.toString()))
            .andExpect(jsonPath("$.followee.name").value("팔로이닉네임"));
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공")
    void getFollowers_success() throws Exception {
        UUID followeeId = UUID.randomUUID();

        AuthorDto follower = AuthorDto.builder()
            .userId(UUID.randomUUID())
            .name("팔로워닉네임")
            .profileImageUrl("profile.png")
            .build();

        AuthorDto followee = AuthorDto.builder()
            .userId(followeeId)
            .name("팔로이닉네임")
            .profileImageUrl("followee.png")
            .build();

        FollowDto dto = FollowDto.builder()
            .id(UUID.randomUUID())
            .follower(follower)
            .followee(followee)
            .build();

        CursorPageResponseDto<FollowDto> mockResponse = new CursorPageResponseDto<>(
            List.of(dto),
            "cursor123",
            UUID.randomUUID(),
            false,
            1L,
            null,
            null
        );

        given(followService.getFollowers(eq(followeeId), any(), any(), anyInt(), any()))
            .willReturn(mockResponse);

        mockMvc.perform(get("/api/follows/followers")
                .param("followeeId", followeeId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].follower.name").value("팔로워닉네임"))
            .andExpect(jsonPath("$.data[0].followee.userId").value(followeeId.toString()));
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공")
    void getFollowings_success() throws Exception {
        UUID followerId = UUID.randomUUID();

        AuthorDto follower = AuthorDto.builder()
            .userId(followerId)
            .name("팔로워닉네임")
            .profileImageUrl("profile.png")
            .build();

        AuthorDto followee = AuthorDto.builder()
            .userId(UUID.randomUUID())
            .name("팔로이닉네임")
            .profileImageUrl("followee.png")
            .build();

        FollowDto dto = FollowDto.builder()
            .id(UUID.randomUUID())
            .follower(follower)
            .followee(followee)
            .build();

        CursorPageResponseDto<FollowDto> mockResponse = new CursorPageResponseDto<>(
            List.of(dto),
            "cursor456",
            UUID.randomUUID(),
            false,
            1L,
            null,
            null
        );

        given(followService.getFollowings(eq(followerId), any(), any(), anyInt(), any()))
            .willReturn(mockResponse);

        mockMvc.perform(get("/api/follows/followings")
                .param("followerId", followerId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].followee.name").value("팔로이닉네임"))
            .andExpect(jsonPath("$.data[0].follower.userId").value(followerId.toString()));
    }

    @Test
    @DisplayName("언팔로우 성공")
    void deleteFollow_success() throws Exception {
        UUID followId = UUID.randomUUID();
        doNothing().when(followService).deleteFollow(eq(followId));

        mockMvc.perform(delete("/api/follows/{followId}", followId))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("언팔로우 실패 - FollowNotFoundException 발생")
    void deleteFollow_fail_notFound() throws Exception {
        UUID followId = UUID.randomUUID();
        doThrow(new FollowNotFoundException(ErrorCode.FOLLOW_NOT_FOUND))
            .when(followService).deleteFollow(eq(followId));

        mockMvc.perform(delete("/api/follows/{followId}", followId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.exceptionName").value("FollowNotFoundException"));
    }

    @Test
    @DisplayName("팔로우 요약 조회 성공")
    void getFollowSummary_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID followedByMeId = UUID.randomUUID();

        FollowSummaryDto response = FollowSummaryDto.builder()
            .followeeId(userId)
            .followerCount(10L)
            .followingCount(7L)
            .followedByMe(true)
            .followedByMeId(followedByMeId)
            .followingMe(false)
            .build();

        given(followService.getFollowSummary(eq(userId))).willReturn(response);

        mockMvc.perform(get("/api/follows/summary")
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.followeeId").value(userId.toString()))
            .andExpect(jsonPath("$.followerCount").value(10))
            .andExpect(jsonPath("$.followingCount").value(7))
            .andExpect(jsonPath("$.followedByMe").value(true))
            .andExpect(jsonPath("$.followedByMeId").value(followedByMeId.toString()))
            .andExpect(jsonPath("$.followingMe").value(false));
    }
}
