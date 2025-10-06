package com.onepiece.otboo.domain.feed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.service.FeedQueryService;
import com.onepiece.otboo.domain.feed.service.FeedService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = FeedController.class)
class FeedControllerListTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FeedQueryService feedQueryService;

    @MockitoBean
    FeedService feedService;

    private CursorPageResponseDto<FeedResponse> 빈응답() {
        return new CursorPageResponseDto<>(
            List.of(), null, null, false, 0L, SortBy.CREATED_AT, SortDirection.DESCENDING
        );
    }

    @Test
    @DisplayName("[비인증] 필수 파라미터만 전달 → 200 OK")
    void 비인증_필수파라미터만_OK_그리고_me_null() throws Exception {
        given(feedQueryService.listFeeds(any(), any(), anyInt(), any(), any(),
            any(), any(), any(), any(), isNull())).willReturn(빈응답());

        mockMvc.perform(get("/api/feeds")
                .queryParam("limit", "20")
                .queryParam("sortBy", "CREATED_AT")
                .queryParam("sortDirection", "DESCENDING")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$.totalCount").value(0))
            .andExpect(jsonPath("$.sortBy").value("CREATED_AT"))
            .andExpect(jsonPath("$.sortDirection").value("DESCENDING"));

        ArgumentCaptor<UUID> meCap = ArgumentCaptor.forClass(UUID.class);
        verify(feedQueryService).listFeeds(
            isNull(), isNull(), eq(20), eq(SortBy.CREATED_AT), eq(SortDirection.DESCENDING),
            isNull(), isNull(), isNull(), isNull(), meCap.capture()
        );
        assert meCap.getValue() == null;
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    @DisplayName("[인증] 모든 파라미터 전달 → 200 OK")
    void 인증_전체파라미터_OK_그리고_me_UUID() throws Exception {
        given(feedQueryService.listFeeds(any(), any(), anyInt(), any(), any(),
            any(), any(), any(), any(), any())).willReturn(빈응답());

        var cursor = Instant.now().toString();
        var idAfter = UUID.randomUUID().toString();
        var authorId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/feeds")
                .queryParam("cursor", cursor)
                .queryParam("idAfter", idAfter)
                .queryParam("limit", "10")
                .queryParam("sortBy", "likeCount")
                .queryParam("sortDirection", "ASCENDING")
                .queryParam("keywordLike", "후드티")
                .queryParam("skyStatusEqual", "CLEAR")
                .queryParam("precipitationTypeEqual", "NONE")
                .queryParam("authorIdEqual", authorId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.sortBy").value("CREATED_AT")) // 빈응답()의 값
            .andExpect(jsonPath("$.sortDirection").value("DESCENDING"));

        ArgumentCaptor<String> cursorCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UUID> idAfterCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<SortBy> sortByCap = ArgumentCaptor.forClass(SortBy.class);
        ArgumentCaptor<SortDirection> sortDirCap = ArgumentCaptor.forClass(SortDirection.class);
        ArgumentCaptor<String> keywordCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> skyCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> precipCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UUID> authorCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UUID> meCap = ArgumentCaptor.forClass(UUID.class);

        verify(feedQueryService).listFeeds(
            cursorCap.capture(),
            idAfterCap.capture(),
            limitCap.capture(),
            sortByCap.capture(),
            sortDirCap.capture(),
            keywordCap.capture(),
            skyCap.capture(),
            precipCap.capture(),
            authorCap.capture(),
            meCap.capture()
        );

        assert cursorCap.getValue().equals(cursor);
        assert idAfterCap.getValue().toString().equals(idAfter);
        assert limitCap.getValue() == 10;
        assert sortByCap.getValue() == SortBy.LIKE_COUNT;
        assert sortDirCap.getValue() == SortDirection.ASCENDING;
        assert keywordCap.getValue().equals("후드티");
        assert skyCap.getValue().equals("CLEAR");
        assert precipCap.getValue().equals("NONE");
        assert authorCap.getValue().toString().equals(authorId);
        assert meCap.getValue().toString().equals("11111111-1111-1111-1111-111111111111");
    }
}
