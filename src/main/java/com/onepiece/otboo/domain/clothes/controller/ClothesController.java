package com.onepiece.otboo.domain.clothes.controller;

import com.onepiece.otboo.domain.clothes.api.ClothesApi;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.service.ClothesService;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 의상 관리 컨트롤러
 * 사용자의 의상 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
@Validated
public class ClothesController implements ClothesApi {

  private final ClothesService clothesService;

  @GetMapping
  @PreAuthorize("#ownerId == principal.userId")
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

    CursorPageResponseDto<ClothesDto> response = clothesService.getClothes(
        ownerId, cursor, idAfter, limit, sortBy, sortDirection, typeEqual);

    return ResponseEntity.ok(response);
  }

  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ClothesDto> createClothes(
        @Valid @RequestPart ClothesCreateRequest request,
        @RequestPart(value= "image", required = false) MultipartFile imageFile
    ) throws IOException {
      // 인증된 사용자 ID 가져오기
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      UUID authenticatedUserId = resolveRequesterId(auth);

      log.info("의상 등록 API 호출 - request: {}", request);

      // request의 ownerId와 인증된 사용자 ID 비교
      if (!request.ownerId().equals(authenticatedUserId)) {
          log.warn("권한 없음 - 요청한 ownerId: {}, 인증된 userId: {}", request.ownerId(), authenticatedUserId);
          throw new GlobalException(ErrorCode.FORBIDDEN);
      }

      ClothesDto clothes = clothesService.createClothes(request, imageFile);

      return ResponseEntity.ok(clothes);
    }

    private UUID resolveRequesterId(Authentication auth) {
        if (auth == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUserId();
        }
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
    }
}
