package com.onepiece.otboo.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.profile.dto.request.ProfileUpdateRequest;
import com.onepiece.otboo.domain.profile.dto.response.ProfileDto;
import com.onepiece.otboo.domain.profile.enums.Gender;
import com.onepiece.otboo.domain.profile.fixture.ProfileDtoFixture;
import com.onepiece.otboo.domain.profile.service.ProfileService;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ActiveProfiles("prod")
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileService profileService;

    private ProfileUpdateRequest req;

    @BeforeEach
    void setUp() {
        req = new ProfileUpdateRequest("한동우", Gender.MALE,
            LocalDate.of(1999, 7, 2), null,
            5);
    }

    private CustomUserDetails customUserDetails(UUID id, String role) {
        return new CustomUserDetails(
            id,
            "me@test.com",
            "{noop}pw",
            Role.valueOf(role),
            false,
            null,
            null
        );
    }

    @Test
    void 본인이_자신의_프로필_조회_요청시_200을_반환한다() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        ProfileDto profileDto = ProfileDtoFixture.createProfile(userId);

        given(profileService.getUserProfile(userId)).willReturn(profileDto);

        CustomUserDetails principal = customUserDetails(userId, "USER");

        // when
        ResultActions result = mockMvc.perform(get("/api/users/{userId}/profiles", userId)
            .with(user(principal)));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void USER_권한을_가진_사용자는_다른_사용자의_프로필_조회_요청시_403을_반환한다() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        CustomUserDetails principal = customUserDetails(userId, "USER");

        // when
        ResultActions result = mockMvc.perform(get("/api/users/{userId}/profiles", userId2)
            .with(user(principal)));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    void ADMIN_권한을_가진_사용자는_다른_사용자의_프로필_조회_요청시_200을_반환한다() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        ProfileDto profileDto = ProfileDtoFixture.createProfile(userId2);

        given(profileService.getUserProfile(userId2)).willReturn(profileDto);

        CustomUserDetails principal = customUserDetails(userId, "ADMIN");

        // when
        ResultActions result = mockMvc.perform(get("/api/users/{userId}/profiles", userId2)
            .with(user(principal)));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(userId2.toString()));
    }

    @Test
    void 본인의_프로필_수정_요청시_200을_반환한다() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        ProfileDto profileDto = ProfileDtoFixture.createProfile(userId);

        CustomUserDetails principal = customUserDetails(userId, "USER");

        given(profileService.update(any(UUID.class), any(), any())).willReturn(profileDto);

        byte[] json = objectMapper.writeValueAsBytes(req);
        MockPart requestPart = new MockPart("request", json);
        requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // when
        ResultActions result = callPatch(userId, principal, req);

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void 다른_사용자의_프로필_수정_요청시_403을_반환한다() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        ProfileDto profileDto = ProfileDtoFixture.createProfile(userId);

        CustomUserDetails principal = customUserDetails(userId2, "USER");

        given(profileService.update(any(UUID.class), any(), any())).willReturn(profileDto);

        // when
        ResultActions result = callPatch(userId, principal, req);

        // then
        result.andExpect(status().isForbidden());
    }

    private MockPart jsonPart(Object body) throws Exception {
        byte[] json = objectMapper.writeValueAsBytes(body);
        MockPart part = new MockPart("request", json);
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return part;
    }

    private ResultActions callPatch(UUID pathUserId, CustomUserDetails principal, Object reqPart)
        throws Exception {
        return mockMvc.perform(
            multipart("/api/users/{userId}/profiles", pathUserId)
                .part(jsonPart(reqPart))
                .with(rb -> {
                    rb.setMethod("PATCH");
                    return rb;
                })
                .with(user(principal))
                .with(csrf()));
    }
}
