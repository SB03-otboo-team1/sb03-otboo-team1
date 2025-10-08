package com.onepiece.otboo.domain.clothes.controller;

import com.onepiece.otboo.domain.clothes.controller.api.ClothesAttributeDefApi;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.onepiece.otboo.domain.clothes.service.ClothesAttributeDefService;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/clothes/attribute-defs")
@RequiredArgsConstructor
@Validated
public class ClothesAttributeDefController implements ClothesAttributeDefApi {

    private final ClothesAttributeDefService clothesAttributeDefService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClothesAttributeDefDto>> getClothesAttributeDefs(
        @RequestParam(defaultValue = "createdAt") SortBy sortBy,
        @RequestParam(defaultValue = "ASCENDING") SortDirection sortDirection,
        @RequestParam(required = false) String keywordLike
    ) {
        log.info("의상 속성 조회 API 호출");

        List<ClothesAttributeDefDto> response =
            clothesAttributeDefService.getClothesAttributeDefs(
            sortBy, sortDirection, keywordLike);

        log.info("의상 속성 조회 작업 완료");

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClothesAttributeDefDto> createClothesAttributeDef(
        @Valid @RequestBody ClothesAttributeDefCreateRequest request
    ) {
        log.info("의상 속성 정의 등록 API 실행 - request: {}", request);

        ClothesAttributeDefDto result =
            clothesAttributeDefService.createClothesAttributeDef(request);

        return ResponseEntity.ok(result);
    }

    @PatchMapping(path = "/{definitionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClothesAttributeDefDto> updateClothesAttributeDef(
        @PathVariable UUID definitionId,
        @Valid @RequestBody ClothesAttributeDefUpdateRequest request
    ) {
        log.info("의상 속성 정의 수정 API 실행 - definitionId: {}, request: {}", definitionId, request);

        ClothesAttributeDefDto result =
            clothesAttributeDefService.updateClothesAttributeDef(definitionId, request);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping(path = "/{definitionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClothesAttributeDef(
        @PathVariable UUID definitionId
    ) {
        log.info("의상 속성 정의 삭제 API 실행 - definitionId: {}", definitionId);

        clothesAttributeDefService.deleteClothesAttributeDef(definitionId);

        return ResponseEntity.noContent().build();
    }
}
