package com.onepiece.otboo.domain.clothes.controller;

import com.onepiece.otboo.domain.clothes.controller.api.ClothesAttributeDefApi;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.domain.clothes.service.ClothesAttributeDefService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.enums.SortBy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<CursorPageResponseDto<ClothesAttributeDefDto>> getClothesAttributeDefs(
        @RequestParam(defaultValue = "createdAt") SortBy sortBy,
        @RequestParam(defaultValue = "ASCENDING") SortDirection sortDirection,
        @RequestParam(required = false) String keywordLike
    ) {
        log.info("의상 속성 등록 API 호출");

        CursorPageResponseDto<ClothesAttributeDefDto> response =
            clothesAttributeDefService.getClothesAttributeDefsWithCursor(
            sortBy, sortDirection, keywordLike);

        return ResponseEntity.ok(response);
    }
}
