package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.repository.ClothesCustomRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClotheServiceImpl implements ClothesService {

  private final ClothesCustomRepository clothesCustomRepository;

  public CursorPageResponseDto<ClothesDto> getClothes(UUID ownerId, String cursor, UUID idAfter, int limit, String sortBy, String sortDirection, ClothesType typeEqual) {

    CursorPageResponseDto<ClothesDto> result =
        clothesCustomRepository.findCursorPage(ownerId, cursor, idAfter, limit, sortBy, sortDirection, typeEqual);

    log.info("옷 목록 조회 완료 - ownerId: {}, limit: {}, 전체 데이터 개수: {}", ownerId, limit, result.totalCount());

    return result;
  }
}
