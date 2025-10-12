package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.mapper.ClothesAttributeMapper;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeOptionsRepository;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClothesAttributeDefServiceImpl implements ClothesAttributeDefService{

    private final ClothesAttributeDefRepository clothesAttributeDefRepository;
    private final ClothesAttributeOptionsRepository clothesAttributeOptionsRepository;
    private final ClothesAttributeMapper clothesAttributeMapper;


    @Override
    public List<ClothesAttributeDefDto> getClothesAttributeDefs(
        SortBy sortBy, SortDirection sortDirection, String keywordLike
    ) {
        log.info("의상 속성 조회 시작: sortBy: {}, sortDirection: {}, keywordLike: {}", sortBy, sortDirection, keywordLike);

        List<ClothesAttributeDefs> defs =
            clothesAttributeDefRepository.getClothesAttributeDefs(
                sortBy, sortDirection, keywordLike
            );

        Long totalCount = clothesAttributeDefRepository.countClothesAttributeDefs(keywordLike);

        log.info("의상 속성 목록 조회 완료 - sortBy: {}, sortDirection: {}, keywordLike: {}, 전체 데이터 개수: {}", sortBy, sortDirection, keywordLike, totalCount);

        return clothesAttributeMapper.toAttributeDefDto(defs);
    }
}
