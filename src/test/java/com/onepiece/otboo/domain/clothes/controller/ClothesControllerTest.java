package com.onepiece.otboo.domain.clothes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.service.ClothesService;
import com.onepiece.otboo.global.config.JpaConfig;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.infra.security.testconfig.TestSecurityConfig;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ClothesController.class,
    excludeAutoConfiguration = {JpaConfig.class}
)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class ClothesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClothesService clothesService;

    @Test
    void 의상_목록_조회_성공() throws Exception {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID clothesId = UUID.randomUUID();

        ClothesDto clothesDto = ClothesDto.builder()
            .id(clothesId)
            .ownerId(ownerId)
            .name("테스트 티셔츠")
            .imageUrl("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test-key")
            .type(ClothesType.TOP)
            .build();

        CursorPageResponseDto<ClothesDto> response = new CursorPageResponseDto<>(
            List.of(clothesDto),
            "next-cursor",
            UUID.randomUUID(),
            false,
            1L,
            SortBy.CREATED_AT,
            SortDirection.DESCENDING
        );

        given(clothesService.getClothesWithCursor(eq(ownerId), any(), any(), eq(15),
            eq(SortBy.CREATED_AT),
            eq(SortDirection.DESCENDING), any()))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/clothes")
                .param("ownerId", ownerId.toString())
                .param("limit", "15")
                .param("sortBy", "CREATED_AT")
                .param("sortDirection", "DESCENDING")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(clothesId.toString()))
            .andExpect(jsonPath("$.data[0].ownerId").value(ownerId.toString()))
            .andExpect(jsonPath("$.data[0].name").value("테스트 티셔츠"))
            .andExpect(jsonPath("$.data[0].type").value("TOP"))
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 의상_목록_조회_타입_필터링_성공() throws Exception {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID clothesId = UUID.randomUUID();

        ClothesDto clothesDto = ClothesDto.builder()
            .id(clothesId)
            .ownerId(ownerId)
            .name("테스트 바지")
            .imageUrl("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test-key")
            .type(ClothesType.BOTTOM)
            .build();

        CursorPageResponseDto<ClothesDto> response = new CursorPageResponseDto<>(
            List.of(clothesDto),
            null,
            null,
            false,
            1L,
            SortBy.CREATED_AT,
            SortDirection.DESCENDING
        );

        given(clothesService.getClothesWithCursor(eq(ownerId), any(), any(), eq(10),
            eq(SortBy.CREATED_AT),
            eq(SortDirection.DESCENDING), eq(ClothesType.BOTTOM)))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/clothes")
                .param("ownerId", ownerId.toString())
                .param("limit", "10")
                .param("sortBy", "CREATED_AT")
                .param("sortDirection", "DESCENDING")
                .param("typeEqual", "BOTTOM")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].type").value("BOTTOM"))
            .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    void 의상_목록_조회_커서_페이징_성공() throws Exception {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID clothesId1 = UUID.randomUUID();
        UUID clothesId2 = UUID.randomUUID();
        UUID idAfter = UUID.randomUUID();

        ClothesDto clothesDto1 = ClothesDto.builder()
            .id(clothesId1)
            .ownerId(ownerId)
            .name("첫 번째 옷")
            .imageUrl("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test-key-1")
            .type(ClothesType.TOP)
            .build();

        ClothesDto clothesDto2 = ClothesDto.builder()
            .id(clothesId2)
            .ownerId(ownerId)
            .name("두 번째 옷")
            .imageUrl("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test-key-2")
            .type(ClothesType.BOTTOM)
            .build();

        CursorPageResponseDto<ClothesDto> response = new CursorPageResponseDto<>(
            List.of(clothesDto1, clothesDto2),
            "next-cursor",
            clothesId2,
            true,
            10L,
            SortBy.CREATED_AT,
            SortDirection.DESCENDING
        );

        given(
            clothesService.getClothesWithCursor(eq(ownerId), eq("test-cursor"), eq(idAfter), eq(5),
                eq(SortBy.CREATED_AT), eq(SortDirection.DESCENDING), any()))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/clothes")
                .param("ownerId", ownerId.toString())
                .param("cursor", "test-cursor")
                .param("idAfter", idAfter.toString())
                .param("limit", "5")
                .param("sortBy", "CREATED_AT")
                .param("sortDirection", "DESCENDING")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.nextCursor").value("next-cursor"))
            .andExpect(jsonPath("$.nextIdAfter").value(clothesId2.toString()))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andExpect(jsonPath("$.totalCount").value(10));
    }

    @Test
    void 의상_목록_조회_빈_목록_성공() throws Exception {
        // given
        UUID ownerId = UUID.randomUUID();

        CursorPageResponseDto<ClothesDto> response = new CursorPageResponseDto<>(
            List.of(),
            null,
            null,
            false,
            0L,
            SortBy.CREATED_AT,
            SortDirection.DESCENDING
        );

        given(clothesService.getClothesWithCursor(eq(ownerId), any(), any(), eq(15),
            eq(SortBy.CREATED_AT),
            eq(SortDirection.DESCENDING), any()))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/clothes")
                .param("ownerId", ownerId.toString())
                .param("limit", "15")
                .param("sortBy", "CREATED_AT")
                .param("sortDirection", "DESCENDING")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0))
            .andExpect(jsonPath("$.totalCount").value(0))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 의상_목록_조회_필수_파라미터_누락_실패() throws Exception {
        // when & then - ownerId가 없으면 400 에러가 발생해야 함
        mockMvc.perform(get("/api/clothes")
                .param("limit", "15")
                .param("sortBy", "CREATED_AT")
                .param("sortDirection", "DESCENDING")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 의상_목록_조회_잘못된_타입_파라미터_실패() throws Exception {
        // given
        UUID ownerId = UUID.randomUUID();

        // when & then
        mockMvc.perform(get("/api/clothes")
                .param("ownerId", ownerId.toString())
                .param("limit", "15")
                .param("sortBy", "CREATED_AT")
                .param("sortDirection", "DESCENDING")
                .param("typeEqual", "INVALID_TYPE")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 의상_목록_조회_잘못된_UUID_파라미터_실패() throws Exception {
        // when & then
        mockMvc.perform(get("/api/clothes")
                .param("ownerId", "invalid-uuid")
                .param("limit", "15")
                .param("sortBy", "CREATED_AT")
                .param("sortDirection", "DESCENDING")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 의상_목록_조회_음수_limit_파라미터_처리() throws Exception {
        // given
        UUID ownerId = UUID.randomUUID();

        // when & then
        mockMvc.perform(get("/api/clothes")
                .param("ownerId", ownerId.toString())
                .param("limit", "-1")
                .param("sortBy", "CREATED_AT")
                .param("sortDirection", "DESCENDING")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(clothesService);
    }

    @Test
    void 의상_목록_조회_모든_ClothesType_테스트() throws Exception {
        // given
        UUID ownerId = UUID.randomUUID();
        ClothesType[] allTypes = ClothesType.values();

        for (ClothesType type : allTypes) {
            ClothesDto clothesDto = ClothesDto.builder()
                .id(UUID.randomUUID())
                .ownerId(ownerId)
                .name("테스트 " + type.name())
                .imageUrl("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test-key")
                .type(type)
                .build();

            CursorPageResponseDto<ClothesDto> response = new CursorPageResponseDto<>(
                List.of(clothesDto),
                null,
                null,
                false,
                1L,
                SortBy.CREATED_AT,
                SortDirection.DESCENDING
            );

            given(
                clothesService.getClothesWithCursor(eq(ownerId), any(), any(), eq(15),
                    eq(SortBy.CREATED_AT),
                    eq(SortDirection.DESCENDING), eq(type)))
                .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/clothes")
                    .param("ownerId", ownerId.toString())
                    .param("limit", "15")
                    .param("sortBy", "CREATED_AT")
                    .param("sortDirection", "DESCENDING")
                    .param("typeEqual", type.name())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value(type.name()));
        }
    }
}