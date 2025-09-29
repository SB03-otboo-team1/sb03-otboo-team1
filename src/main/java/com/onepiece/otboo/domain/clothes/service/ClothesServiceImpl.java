package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.mapper.ClothesMapper;
import com.onepiece.otboo.domain.clothes.repository.ClothesRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothesServiceImpl implements ClothesService {

  private final ClothesRepository clothesRepository;
  private final ClothesMapper clothesMapper;

  @Override
  public CursorPageResponseDto<ClothesDto> getClothes(UUID ownerId, String cursor, UUID idAfter, int limit, String sortBy, String sortDirection, ClothesType typeEqual) {

    List<Clothes> clothes =
        clothesRepository.getClothesWithCursor(ownerId, cursor, idAfter, limit, sortBy, sortDirection, typeEqual);

      if (clothes == null) {
          clothes = Collections.emptyList();
      }

    boolean hasNext = clothes.size() > limit;

      String nextCursor = null;
      UUID nextIdAfter = null;

      if (hasNext) {
          clothes = clothes.subList(0, limit);
          Clothes lastClothes = clothes.get(limit - 1);
          switch (sortBy) {
              case "createdAt" -> {
                  nextCursor = lastClothes.getCreatedAt().toString();
              }
              case "name" -> {
                  nextCursor = lastClothes.getName();
              }
          }
          nextIdAfter = lastClothes.getId();
      }

    Long totalCount = clothesRepository.countClothes(ownerId, typeEqual);

    List<ClothesDto> data = clothes.stream().map(clothesMapper::toDto).toList();

    log.info("옷 목록 조회 완료 - ownerId: {}, limit: {}, 전체 데이터 개수: {}", ownerId, limit, totalCount);

    return new CursorPageResponseDto<>(
        data,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        sortBy,
        sortDirection
    );
  }
}
