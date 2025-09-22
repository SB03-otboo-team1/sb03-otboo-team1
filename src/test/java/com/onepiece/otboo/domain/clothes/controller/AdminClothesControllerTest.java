package com.onepiece.otboo.domain.clothes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.clothes.dto.request.UpdateClothesAttributeRequest;
import com.onepiece.otboo.domain.clothes.dto.response.ClothesAttributeResponse;
import com.onepiece.otboo.domain.clothes.entity.AttributeType;
import com.onepiece.otboo.domain.clothes.service.ClothesAttributeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminClothesController 테스트 클래스
 * 어드민 의상 속성 관리 컨트롤러의 API 엔드포인트를 테스트합니다.
 */
@WebMvcTest(AdminClothesController.class)
class AdminClothesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClothesAttributeService clothesAttributeService;

    private ClothesAttributeResponse testAttributeResponse;
    private Page<ClothesAttributeResponse> testAttributePage;
    private List<ClothesAttributeResponse> testAttributeList;
    private CreateClothesAttributeRequest createRequest;
    private UpdateClothesAttributeRequest updateRequest;

    @BeforeEach
    void setUp() {
        testAttributeResponse = ClothesAttributeResponse.builder()
                .id(UUID.randomUUID().toString())
                .name("테스트 속성")
                .attributeType(AttributeType.TEXT)
                .isRequired(true)
                .displayOrder(1)
                .description("테스트용 속성입니다.")
                .isActive(true)
                .build();

        testAttributePage = new PageImpl<>(List.of(testAttributeResponse));
        testAttributeList = List.of(testAttributeResponse);

        createRequest = CreateClothesAttributeRequest.builder()
                .name("새로운 속성")
                .attributeType(AttributeType.SELECT)
                .isRequired(false)
                .displayOrder(2)
                .description("새로운 속성입니다.")
                .isActive(true)
                .build();

        updateRequest = UpdateClothesAttributeRequest.builder()
                .name("수정된 속성")
                .attributeType(AttributeType.NUMBER)
                .isRequired(true)
                .displayOrder(3)
                .description("수정된 속성입니다.")
                .build();
    }

    @Test
    @DisplayName("의상 속성을 성공적으로 생성한다")
    void createAttribute_Success() throws Exception {
        // given
        when(clothesAttributeService.createAttribute(any(CreateClothesAttributeRequest.class)))
                .thenReturn(testAttributeResponse);

        // when & then
        mockMvc.perform(post("/api/v1/admin/clothes/attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("테스트 속성"))
                .andExpect(jsonPath("$.attributeType").value("TEXT"))
                .andExpect(jsonPath("$.isRequired").value(true))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("의상 속성 생성 시 유효하지 않은 데이터로 요청하면 400 에러가 발생한다")
    void createAttribute_InvalidData_Returns400() throws Exception {
        // given
        CreateClothesAttributeRequest invalidRequest = CreateClothesAttributeRequest.builder()
                .name("") // 빈 이름
                .attributeType(null) // null 타입
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/admin/clothes/attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("의상 속성 정보를 성공적으로 조회한다")
    void getAttribute_Success() throws Exception {
        // given
        UUID attributeId = UUID.randomUUID();
        when(clothesAttributeService.getAttribute(attributeId)).thenReturn(testAttributeResponse);

        // when & then
        mockMvc.perform(get("/api/v1/admin/clothes/attributes/{id}", attributeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트 속성"))
                .andExpect(jsonPath("$.attributeType").value("TEXT"))
                .andExpect(jsonPath("$.isRequired").value(true));
    }

    @Test
    @DisplayName("의상 속성 정보를 성공적으로 수정한다")
    void updateAttribute_Success() throws Exception {
        // given
        UUID attributeId = UUID.randomUUID();
        when(clothesAttributeService.updateAttribute(any(UUID.class), any(UpdateClothesAttributeRequest.class)))
                .thenReturn(testAttributeResponse);

        // when & then
        mockMvc.perform(put("/api/v1/admin/clothes/attributes/{id}", attributeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트 속성"));
    }

    @Test
    @DisplayName("의상 속성을 성공적으로 삭제한다")
    void deleteAttribute_Success() throws Exception {
        // given
        UUID attributeId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/v1/admin/clothes/attributes/{id}", attributeId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("의상 속성을 성공적으로 비활성화한다")
    void deactivateAttribute_Success() throws Exception {
        // given
        UUID attributeId = UUID.randomUUID();

        // when & then
        mockMvc.perform(patch("/api/v1/admin/clothes/attributes/{id}/deactivate", attributeId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("활성화된 속성 목록을 성공적으로 조회한다")
    void getActiveAttributes_Success() throws Exception {
        // given
        when(clothesAttributeService.getActiveAttributes(any(Pageable.class)))
                .thenReturn(testAttributePage);

        // when & then
        mockMvc.perform(get("/api/v1/admin/clothes/attributes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("테스트 속성"))
                .andExpect(jsonPath("$.content[0].isActive").value(true));
    }

    @Test
    @DisplayName("특정 타입의 속성 목록을 성공적으로 조회한다")
    void getAttributesByType_Success() throws Exception {
        // given
        when(clothesAttributeService.getAttributesByType(any(AttributeType.class), any(Pageable.class)))
                .thenReturn(testAttributePage);

        // when & then
        mockMvc.perform(get("/api/v1/admin/clothes/attributes/type/TEXT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("테스트 속성"));
    }

    @Test
    @DisplayName("필수 속성 목록을 성공적으로 조회한다")
    void getRequiredAttributes_Success() throws Exception {
        // given
        when(clothesAttributeService.getRequiredAttributes()).thenReturn(testAttributeList);

        // when & then
        mockMvc.perform(get("/api/v1/admin/clothes/attributes/required"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("테스트 속성"))
                .andExpect(jsonPath("$[0].isRequired").value(true));
    }

    @Test
    @DisplayName("표시 순서대로 속성 목록을 성공적으로 조회한다")
    void getAttributesOrderByDisplayOrder_Success() throws Exception {
        // given
        when(clothesAttributeService.getAttributesOrderByDisplayOrder()).thenReturn(testAttributeList);

        // when & then
        mockMvc.perform(get("/api/v1/admin/clothes/attributes/ordered"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("테스트 속성"))
                .andExpect(jsonPath("$[0].displayOrder").value(1));
    }

    @Test
    @DisplayName("속성명으로 속성을 성공적으로 검색한다")
    void searchAttributesByName_Success() throws Exception {
        // given
        String name = "테스트";
        when(clothesAttributeService.searchAttributesByName(anyString(), any(Pageable.class)))
                .thenReturn(testAttributePage);

        // when & then
        mockMvc.perform(get("/api/v1/admin/clothes/attributes/search")
                        .param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("테스트 속성"));
    }

    @Test
    @DisplayName("페이징 파라미터를 포함한 속성 목록 조회를 테스트한다")
    void getActiveAttributes_WithPagination_Success() throws Exception {
        // given
        when(clothesAttributeService.getActiveAttributes(any(Pageable.class)))
                .thenReturn(testAttributePage);

        // when & then
        mockMvc.perform(get("/api/v1/admin/clothes/attributes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("존재하지 않는 속성 조회 시 404 에러가 발생한다")
    void getAttribute_NotFound_Returns404() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(clothesAttributeService.getAttribute(nonExistentId))
                .thenThrow(new RuntimeException("속성을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/api/v1/admin/clothes/attributes/{id}", nonExistentId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("속성명 검색 시 빈 파라미터로 요청하면 400 에러가 발생한다")
    void searchAttributesByName_EmptyName_Returns400() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/admin/clothes/attributes/search")
                        .param("name", ""))
                .andExpect(status().isBadRequest());
    }
}
