package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeOptions;
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

    @Override
    public ClothesAttributeDefDto createClothesAttributeDef(ClothesAttributeDefCreateRequest request) {

        log.info("[의상 속성 정의] 등록 작업 시작");

        String name = request.name();
        List<String> selectableValues = request.selectableValues();

        log.debug("[의상 속성 정의] 의상 속성 정의 생성");
        ClothesAttributeDefs def =
            ClothesAttributeDefs.builder()
                .name(name)
                .build();

        log.debug("의상 속성 정의 저장");
        clothesAttributeDefRepository.save(def);

        log.debug("의상 속성 정의 저장 완료 - definitionId: {}", def.getId());

        log.debug("의상 속성값 생성");
        List<ClothesAttributeOptions> options =
            selectableValues.stream().map(
                val -> ClothesAttributeOptions.builder()
                    .definition(def)
                    .optionValue(val)
                    .build()
            ).toList();

        log.debug("의상 속성값 저장");
        clothesAttributeOptionsRepository.saveAll(options);

        log.debug("의상 속성값 저장 완료 - definitionId: {}, countOptions: {}", def.getId(), options.size());

        return clothesAttributeMapper.toAttributeDefDto(def, options);
    }
}
