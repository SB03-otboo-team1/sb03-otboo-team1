package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeOptions;
import com.onepiece.otboo.domain.clothes.mapper.ClothesAttributeMapper;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeOptionsRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothesAttributeDefServiceImpl implements ClothesAttributeDefService{

    private final ClothesAttributeDefRepository clothesAttributeDefRepository;
    private final ClothesAttributeOptionsRepository clothesAttributeOptionsRepository;
    private final ClothesAttributeMapper clothesAttributeMapper;


    @Override
    public CursorPageResponseDto<ClothesAttributeDefDto> getClothesAttributeDefsWithCursor(
        SortBy sortBy, SortDirection sortDirection, String keywordLike
    ) {
        log.info("의상 속성 조회 시작: sortBy: {}, sortDirection: {}, keywordLike: {}", sortBy, sortDirection, keywordLike);

        List<ClothesAttributeDefs> defs =
            clothesAttributeDefRepository.getClothesAttributeDefsWithCursor(
                sortBy, sortDirection, keywordLike
            );

        if (defs == null) {
            defs = Collections.emptyList();
        }

        int limit = 20;
        boolean hasNext = defs.size() > limit;
        if (hasNext) {
            defs = defs.subList(0, limit);
        }

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (hasNext) {
            defs = defs.subList(0, limit);
            ClothesAttributeDefs lastAttributesDef = defs.get(limit - 1);
            switch (sortBy) {
                case CREATED_AT -> {
                    nextCursor = lastAttributesDef.getCreatedAt().toString();
                }
                case NAME -> {
                    nextCursor = lastAttributesDef.getName();
                }
            }
            nextIdAfter = lastAttributesDef.getId();
        }

        Long totalCount = clothesAttributeDefRepository.countClothes(keywordLike);

        log.info("의상 속성 목록 조회 완료 - sortBy: {}, sortDirection: {}, keywordLike: {}, 전체 데이터 개수: {}", sortBy, sortDirection, keywordLike, totalCount);

        // ClotehsAttributeDefDto 조립
        List<ClothesAttributeDefDto> data =
            defs.stream().map(c -> {
                List<ClothesAttributeOptions> options = clothesAttributeOptionsRepository.findByDefinitionId(c.getId());
                return clothesAttributeMapper.toAttributeDefDto(c, options);
            }).toList();

        return new CursorPageResponseDto<>(
            data,
            nextCursor,
            nextIdAfter,
            hasNext,
            totalCount,
            // TODO: CursorPageResponseDto<T> sortBy 필드 타입 변경 필요?
            String.valueOf(sortBy),
            sortDirection
        );
    }
}
