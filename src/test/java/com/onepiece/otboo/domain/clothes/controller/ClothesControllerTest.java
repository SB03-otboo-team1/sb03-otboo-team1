package com.onepiece.otboo.domain.clothes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesUpdateRequest;
import com.onepiece.otboo.domain.clothes.dto.response.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.service.ClothesService;
import com.onepiece.otboo.global.util.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ClothesController 테스트 클래스
 * 의상 관리 컨트롤러의 API 엔드포인트를 테스트합니다.
 */
@WebMvcTest(ClothesController.class)
class ClothesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClothesService clothesService;

    @MockBean
    private S3Service s3Service;

    private ClothesDto testClothesResponse;
    private ClothesCreateRequest createRequest;
    private ClothesUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testClothesResponse = ClothesDto.builder()
                .id(UUID.randomUUID().toString())
                .name("테스트 의상")
                .type(ClothesType.TOP)
                .imageUrl("https://example.com/image.jpg")
                .build();

        testClothesListResponse = CursorPageResponse.builder()
                .clothes(List.of(testClothesResponse))
                .totalCount(1L)
                .page(0)
                .size(20)
                .hasNext(false)
                .build();

        createRequest = ClothesCreateRequest.builder()
                .ownerId(UUID.randomUUID())
                .name("새로운 의상")
                .type(ClothesType.BOTTOM)
                .build();

        updateRequest = ClothesUpdateRequest.builder()
                .name("수정된 의상")
                .type(ClothesType.DRESS)
                .build();
    }

    @Test
    @DisplayName("의상을 성공적으로 등록한다")
    void createClothes_Success() throws Exception {
        // given
        when(clothesService.create(any(ClothesCreateRequest.class))).thenReturn(testClothesResponse);

        // when & then
        mockMvc.perform(post("/api/v1/clothes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("테스트 의상"))
                .andExpect(jsonPath("$.type").value("TOP"))
                .andExpect(jsonPath("$.brand").value("테스트 브랜드"))
                .andExpect(jsonPath("$.price").value(50000));
    }

    @Test
    @DisplayName("의상 등록 시 유효하지 않은 데이터로 요청하면 400 에러가 발생한다")
    void createClothes_InvalidData_Returns400() throws Exception {
        // given
        ClothesCreateRequest invalidRequest = ClothesCreateRequest.builder()
                .name("") // 빈 이름
                .type(null) // null 타입
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/clothes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("의상 정보를 성공적으로 조회한다")
    void getClothes_Success() throws Exception {
        // given
        UUID clothesId = UUID.randomUUID();
        when(clothesService.findById(clothesId)).thenReturn(testClothesResponse);

        // when & then
        mockMvc.perform(get("/api/v1/clothes/{id}", clothesId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트 의상"))
                .andExpect(jsonPath("$.type").value("TOP"))
                .andExpect(jsonPath("$.brand").value("테스트 브랜드"));
    }

    @Test
    @DisplayName("의상 정보를 성공적으로 수정한다")
    void updateClothes_Success() throws Exception {
        // given
        UUID clothesId = UUID.randomUUID();
        when(clothesService.updateById(any(UUID.class), any(ClothesUpdateRequest.class), any(MultipartFile.class)))
                .thenReturn(testClothesResponse);

        // when & then
        mockMvc.perform(put("/api/v1/clothes/{id}", clothesId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트 의상"));
    }

    @Test
    @DisplayName("의상을 성공적으로 삭제한다")
    void deleteClothes_Success() throws Exception {
        // given
        UUID clothesId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/v1/clothes/{id}", clothesId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("의상 목록을 성공적으로 조회한다")
    void getClothesList_Success() throws Exception {
        // given
        when(clothesService.findAll()).thenReturn(testClothesListResponse);

        // when & then
        mockMvc.perform(get("/api/v1/clothes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clothes").isArray())
                .andExpect(jsonPath("$.clothes[0].name").value("테스트 의상"))
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("타입별 의상 목록을 성공적으로 조회한다")
    void getClothesByType_Success() throws Exception {
        // given
        when(clothesService.findByType(any(ClothesType.class)))
                .thenReturn(testClothesListResponse);

        // when & then
        mockMvc.perform(get("/api/v1/clothes/type/TOP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clothes").isArray())
                .andExpect(jsonPath("$.clothes[0].name").value("테스트 의상"));
    }

    @Test
    @DisplayName("의상 이미지를 성공적으로 삭제한다")
    void deleteClothesImage_Success() throws Exception {
        // given
        UUID clothesId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/v1/clothes/{id}/image", clothesId))
                .andExpect(status().isNoContent());
    }
}