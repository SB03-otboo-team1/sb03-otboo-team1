package com.onepiece.otboo.domain.clothes.controller;

import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesUpdateRequest;
import com.onepiece.otboo.domain.clothes.dto.response.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.service.ClothesService;
import com.onepiece.otboo.global.util.S3Service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 의상 관리 컨트롤러
 * 사용자의 의상 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
@Tag(name = "의상 관리", description = "의상 관련 API")
public class ClothesController {

  private final ClothesService clothesService;

  private final S3Service s3Service;

  /**
   * 의상을 등록합니다. (multipart/form-data)
   *
   * @param request 의상 등록 요청
   * @param image 이미지 파일
   * @return 등록된 의상 정보
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "옷 등록", description = "옷 등록 API")
  public ResponseEntity<ClothesDto> createClothes(
      @Parameter(description = "의상 등록 요청") @RequestPart("request") ClothesCreateRequest request,
      @Parameter(description = "이미지 파일") @RequestPart(value = "image", required = false) MultipartFile image) {
    log.info("의상 등록 API 호출: {}", request.getName());
    
    ClothesDto response = clothesService.createClothesWithImageDto(request, image);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * 의상 정보를 조회합니다.
   *
   * @param cursor 커서 (선택사항)
   * @param idAfter ID 이후 (선택사항)
   * @param limit 페이지 크기
   * @param typeEqual 의상 타입 (선택사항)
   * @param ownerId 소유자 ID
   * @param sortBy 정렬 기준
   * @param sortDirection 정렬 방향
   * @return 의상 목록
   */
  @GetMapping
  @Operation(summary = "옷 목록 조회", description = "옷 목록 조회 API")
  public ResponseEntity<CursorPageResponse<ClothesDto>> getClothes(
      @Parameter(description = "커서") @RequestParam(required = false) String cursor,
      @Parameter(description = "ID 이후") @RequestParam(required = false) UUID idAfter,
      @Parameter(description = "페이지 크기") @RequestParam(required = true) int limit,
      @Parameter(description = "의상 타입") @RequestParam(required = false) ClothesType typeEqual,
      @Parameter(description = "소유자 ID") @RequestParam(required = true) UUID ownerId,
      @Parameter(description = "정렬 기준") @RequestParam(required = true) String sortBy,
      @Parameter(description = "정렬 방향") @RequestParam(required = true) String sortDirection) {
    log.info("의상 목록 조회 API 호출 - 소유자: {}, 타입: {}", ownerId, typeEqual);

    CursorPageResponse<ClothesDto> response = clothesService.findAll(
        ownerId, cursor, idAfter, limit, sortBy, sortDirection, typeEqual);
    return ResponseEntity.ok(response);
  }

  /**
   * 의상 정보를 수정합니다. (multipart/form-data)
   *
   * @param clothesId 의상 ID
   * @param request 의상 수정 요청
   * @param image 이미지 파일
   * @return 수정된 의상 정보
   */
  @PatchMapping(value = "/{clothesId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "옷 수정", description = "옷 수정 API")
  public ResponseEntity<ClothesDto> updateClothes(
      @Parameter(description = "의상 ID") @PathVariable UUID clothesId,
      @Parameter(description = "의상 수정 요청") @RequestPart("request") ClothesUpdateRequest request,
      @Parameter(description = "이미지 파일") @RequestPart(value = "image", required = false) MultipartFile image) {
    log.info("의상 수정 API 호출: {}", clothesId);
    
    ClothesDto response = clothesService.updateById(clothesId, request, image);
    return ResponseEntity.ok(response);
  }

  /**
   * 의상을 삭제합니다.
   *
   * @param clothesId 의상 ID
   * @return 삭제 결과
   */
  @DeleteMapping("/{clothesId}")
  @Operation(summary = "옷 삭제", description = "옷 삭제 API")
  public ResponseEntity<Void> deleteClothes(
      @Parameter(description = "의상 ID") @PathVariable UUID clothesId) {
    log.info("의상 삭제 API 호출: {}", clothesId);
    
    clothesService.deleteById(clothesId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 수동으로 의상을 등록합니다. (이미지 파일과 함께)
   *
   * @param request 의상 등록 요청
   * @param imageFile 이미지 파일
   * @return 등록된 의상 정보
   */
  @PostMapping(value = "/manual", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "수동 의상 등록", description = "이미지 파일과 함께 의상을 수동으로 등록합니다.")
  public ResponseEntity<ClothesDto> createClothesManually(
      @Parameter(description = "의상 등록 요청") @RequestPart("request") ClothesCreateRequest request,
      @Parameter(description = "이미지 파일") @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
    log.info("수동 의상 등록 API 호출: {}", request.getName());
    
    ClothesDto response = clothesService.create(request, imageFile);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * 의상 이미지를 S3에 업로드합니다.
   *
   * @param id 의상 ID
   * @param imageFile 이미지 파일
   * @return 업로드된 이미지 URL
   */
  @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "의상 이미지 업로드", description = "의상 이미지를 S3에 업로드합니다.")
  public ResponseEntity<String> uploadClothesImage(
      @Parameter(description = "의상 ID") @PathVariable UUID id,
      @Parameter(description = "이미지 파일") @RequestPart("imageFile") MultipartFile imageFile) {
    log.info("의상 이미지 업로드 API 호출: {}", id);
    
    String imageUrl = clothesService.findById(id).imageUrl();
    s3Service.uploadFile(imageFile);
    return ResponseEntity.ok(imageUrl);
  }

  /**
   * 의상 이미지를 S3에서 삭제합니다.
   *
   * @param id 의상 ID
   * @return 삭제 결과
   */
  @DeleteMapping("/{id}/image")
  @Operation(summary = "의상 이미지 삭제", description = "의상 이미지를 S3에서 삭제합니다.")
  public ResponseEntity<Void> deleteClothesImage(
      @Parameter(description = "의상 ID") @PathVariable UUID id) {
    log.info("의상 이미지 삭제 API 호출: {}", id);
    
    String imageUrl = clothesService.findById(id).imageUrl();
    s3Service.deleteFile(imageUrl);
    return ResponseEntity.noContent().build();
  }
}
