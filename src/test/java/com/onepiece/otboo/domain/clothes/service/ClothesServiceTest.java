package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesUpdateRequest;
import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.exception.ClothesNotFoundException;
import com.onepiece.otboo.domain.clothes.mapper.ClothesMapper;
import com.onepiece.otboo.domain.clothes.repository.ClothesRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.util.S3Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ClothesService 테스트 클래스
 * 의상 서비스의 비즈니스 로직을 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
class ClothesServiceTest {

    @Mock
    private ClothesRepository clothesRepository;

    @Mock
    private ClothesMapper clothesMapper;

    @InjectMocks
    private ClothesService clothesService;

    @Mock
    private S3Service s3Service;

    private Clothes testClothes;
    private ClothesDto testClothesResponse;
    private ClothesCreateRequest createRequest;
    private ClothesUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testClothes = Clothes.builder()
                .name("테스트 의상")
                .type(ClothesType.TOP)
                .imageUrl("https://example.com/image.jpg")
                .build();

        testClothesResponse = ClothesDto.builder()
                .id(UUID.randomUUID().toString())
                .name("테스트 의상")
                .type(ClothesType.TOP)
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
    void 의상_목록을_성공적으로_조회한다() {
        // given
        UUID clothesId = UUID.randomUUID();
        when(clothesRepository.findById(clothesId)).thenReturn(Optional.of(testClothes));
        when(clothesRepository.findAll()).thenReturn(clothesPage);
        when(clothesMapper.toDto(any(Clothes.class))).thenReturn(testClothesResponse);

        // when
        CursorPageResponseDto<ClothesDto> result = clothesService.findAll();

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("테스트 의상");
        verify(clothesRepository).findById(clothesId);
        verify(clothesMapper).toDto(testClothes);
    }

    @Test
    @DisplayName("존재하지 않는 의상 조회 시 예외가 발생한다")
    void getClothes_NotFound_ThrowsException() {
        // given
        UUID clothesId = UUID.randomUUID();
        when(clothesRepository.findById(clothesId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clothesService.findById(clothesId))
                .isInstanceOf(ClothesNotFoundException.class)
                .hasMessage("의상을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("타입별 의상 목록을 성공적으로 조회한다")
    void getClothesByType_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        CursorPageResponse<ClothesDto> clothesPage = new PageImpl<>(List.of(testClothesResponse));
        when(clothesRepository.findByType(ClothesType.TOP)).thenReturn(clothesPage);
        when(clothesMapper.toDto(any(Clothes.class))).thenReturn(testClothesResponse);

        // when
        CursorPageResponse<ClothesDto> result = clothesService.findAll();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getClothes()).hasSize(1);
        verify(clothesRepository).findByType(ClothesType.TOP);
    }

    @Test
    void 의상을_성공적으로_등록한다() {
        // given
        MultipartFile mockImageFile = mock(MultipartFile.class);
        when(mockImageFile.isEmpty()).thenReturn(false);
        when(mockImageFile.getOriginalFilename()).thenReturn("test.jpg");
        when(s3Service.isImageFile(mockImageFile)).thenReturn(true);
        when(s3Service.validateFileSize(mockImageFile, 10)).thenReturn(true);
        when(s3Service.uploadFile(mockImageFile)).thenReturn("https://s3.amazonaws.com/test-image.jpg");
        when(clothesRepository.save(any(Clothes.class))).thenReturn(testClothes);
        when(clothesMapper.toDto(any(Clothes.class))).thenReturn(testClothesResponse);

        // when
        ClothesDto result = clothesService.create(createRequest, mockImageFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("테스트 의상");
        verify(s3Service).uploadFile(mockImageFile);
        verify(clothesRepository).save(any(Clothes.class));
    }

    @Test
    void 수동_의상_등록_시_유효하지_않은_이미지_파일로_예외가_발생한다() {
        // given
        MultipartFile mockImageFile = mock(MultipartFile.class);
        when(mockImageFile.isEmpty()).thenReturn(false);
        when(s3Service.isImageFile(mockImageFile)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> clothesService.create(createRequest, mockImageFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미지 파일만 업로드 가능합니다.");
    }

    @Test
    void 의상을_옷장에서_성공적으로_삭제한다() {
        // given
        UUID clothesId = UUID.randomUUID();
        when(clothesRepository.findById(clothesId)).thenReturn(Optional.of(testClothes));
        when(clothesRepository.save(any(Clothes.class))).thenReturn(testClothes);

        // when
        clothesService.deleteById(clothesId);

        // then
        verify(s3Service).deleteFile("https://s3.amazonaws.com/test-image.jpg");
        verify(clothesRepository).save(testClothes);
    }

    @Test
    void 존재하지_않는_의상의_이미지_삭제_시_예외가_발생한다() {
        // given
        UUID clothesId = UUID.randomUUID();
        when(clothesRepository.findById(clothesId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clothesService.deleteById(clothesId))
                .isInstanceOf(ClothesNotFoundException.class)
                .hasMessage("의상을 찾을 수 없습니다.");
    }
}