package com.onepiece.otboo.domain.clothes.service;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.onepiece.otboo.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeOptions;
import com.onepiece.otboo.domain.clothes.exception.ClothesAttributeDefNotFoundException;
import com.onepiece.otboo.domain.clothes.mapper.ClothesAttributeMapper;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeOptionsRepository;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
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

        List<ClothesAttributeDefDto> result =
            defs.stream().map(
                def ->
                    clothesAttributeMapper.toAttributeDefDto(
                        def,
                        clothesAttributeOptionsRepository.findByDefinitionId(def.getId())
                    )
            ).toList();

        Long totalCount = clothesAttributeDefRepository.countClothesAttributeDefs(keywordLike);

        log.info("의상 속성 목록 조회 완료 - sortBy: {}, sortDirection: {}, keywordLike: {}, 전체 데이터 개수: {}", sortBy, sortDirection, keywordLike, totalCount);

        return result;
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

    @Override
    public ClothesAttributeDefDto updateClothesAttributeDef(
        UUID definitionId,
        ClothesAttributeDefUpdateRequest request
    ) {

        log.info("[의상 속성 정의] 수정 작업 시작");

        ClothesAttributeDefs oldDef =
            clothesAttributeDefRepository.findById(definitionId)
                .orElseThrow(() -> new ClothesAttributeDefNotFoundException("의상 속성을 찾을 수 없습니다"));

        List<ClothesAttributeOptions> oldOptions = clothesAttributeOptionsRepository.findByDefinitionId(
            definitionId);

        String newName = request.name();
        List<String> selectableValues = request.selectableValues();

        log.debug("의상 속성 정의 이름 수정");
        oldDef.update(newName);

        log.debug("수정된 의상 속성 정의 저장");
        ClothesAttributeDefs newDef = clothesAttributeDefRepository.save(oldDef);

        selectableValues.forEach(val -> {
            if (!oldOptions.stream().anyMatch(o -> o.getOptionValue().equals(val))) {
                ClothesAttributeOptions option = ClothesAttributeOptions.builder()
                    .definition(newDef)
                    .optionValue(val)
                    .build();

                log.debug("새로운 의상 속성값 저장");
                clothesAttributeOptionsRepository.save(option);
            }
        });

        oldOptions.forEach(o -> {
            if (!selectableValues.contains(o.getOptionValue())) {
                clothesAttributeOptionsRepository.deleteById(o.getId());
            }
        });

        List<ClothesAttributeOptions> newOptions = clothesAttributeOptionsRepository.findByDefinitionId(
            definitionId);

        log.debug("의상 속성 정의 수정 완료 - definitionId: {}, countOptions: {}", newDef.getId(),
            newOptions.size());

        return clothesAttributeMapper.toAttributeDefDto(newDef, newOptions);
    }
}
