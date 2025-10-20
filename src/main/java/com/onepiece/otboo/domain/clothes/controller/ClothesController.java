package com.onepiece.otboo.domain.clothes.controller;

import com.onepiece.otboo.domain.clothes.controller.api.ClothesApi;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesUpdateRequest;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.service.ClothesService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 의상 관리 컨트롤러 사용자의 의상 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
@Validated
public class ClothesController implements ClothesApi {

    private final ClothesService clothesService;

    @GetMapping
    public ResponseEntity<CursorPageResponseDto<ClothesDto>> getClothes(
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam(defaultValue = "15") @Positive @Min(1) int limit,
        @RequestParam(required = false) ClothesType typeEqual,
        @RequestParam UUID ownerId
    ) {
        log.info("의상 목록 조회 API 호출 - 소유자: {}, limit: {}", ownerId, limit);

        SortBy sortBy = SortBy.CREATED_AT;
        SortDirection sortDirection = SortDirection.DESCENDING;

        CursorPageResponseDto<ClothesDto> response = clothesService.getClothesWithCursor(
            ownerId, cursor, idAfter, limit, sortBy, sortDirection, typeEqual);

        log.info("의상 목록 조회 작업 완료 - response: {}", response);

        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ClothesDto> createClothes(
        @Valid @RequestPart ClothesCreateRequest request,
        @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {

        log.info("의상 등록 API 호출 - request: {}", request);

        // 인증된 사용자와 일치하는지 확인
        if (!SecurityUtil.isRequester(request.ownerId())) {
            throw new AccessDeniedException("Forbidden");
        }

        ClothesDto clothes = clothesService.createClothes(request, imageFile);

        log.info("의상 등록 작업 완료 - clothes: {}", clothes);

        return ResponseEntity.status(HttpStatus.CREATED).body(clothes);
    }

    @PatchMapping(path = "/{clothesId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ClothesDto> updateClothes(
        @PathVariable UUID clothesId,
        @Valid @RequestPart ClothesUpdateRequest request,
        @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {

        log.info("의상 수정 API 호출 - request: {}", request);

        ClothesDto oldClothes = clothesService.getClothes(clothesId);
        UUID ownerId = oldClothes.ownerId();

        // 인증된 사용자와 일치하는지 확인
        if (!SecurityUtil.isRequester(ownerId)) {
            throw new AccessDeniedException("Forbidden");
        }

        ClothesDto clothes = clothesService.updateClothes(clothesId, request, imageFile);

        log.info("의상 수정 작업 완료 - clothes: {}", clothes);

        return ResponseEntity.ok(clothes);
    }

    @DeleteMapping(path = "/{clothesId}")
    public ResponseEntity<Void> deleteClothes(
        @PathVariable UUID clothesId
    ) {
        log.info("의상 삭제 API 호출 - clothesId: {}", clothesId);

        ClothesDto clothes = clothesService.getClothes(clothesId);
        UUID ownerId = clothes.ownerId();

        // 인증된 사용자와 일치하는지 확인
        if (!SecurityUtil.isRequester(ownerId)) {
            throw new AccessDeniedException("Forbidden");
        }

        clothesService.deleteClothes(clothesId);

        log.info("의상 삭제 작업 완료 - clothesId: {}", clothesId);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    @GetMapping(path = "/extractions")
    public ResponseEntity<ClothesDto> getClothesByUrl(
        @RequestParam String url
    ) throws IOException {
        log.info("구매 링크로 옷 정보 불러오기 API 호출 - url: {}", url);

        // 인증된 사용자 userId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = SecurityUtil.requireAuthorizedUser(auth);

        ClothesDto clothes = clothesService.getClothesByUrl(userId, url);

        log.info("구매 링크로 옷 정보 불러오기 작업 완료 - clothes: {}", clothes);

        return ResponseEntity.ok(clothes);
    }
}
