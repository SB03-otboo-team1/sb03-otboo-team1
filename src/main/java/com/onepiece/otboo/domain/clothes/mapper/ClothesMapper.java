package com.onepiece.otboo.domain.clothes.mapper;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeOptions;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributes;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeOptionsRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeRepository;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.global.storage.S3Storage;
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {ClothesAttributeMapper.class})
public interface ClothesMapper {

    @Mapping(target = "ownerId", source = "clothes.owner.id")
    @Mapping(target = "imageUrl",
        source = "clothes.imageUrl",
        qualifiedByName = "toPublicUrl")
    @Mapping(target = "attributes",
        source = "clothes",
        qualifiedByName = "mapAttributes")
    ClothesDto toDto(Clothes clothes,
        @Context FileStorage fileStorage,
        @Context ClothesAttributeDefRepository defsRepository,
        @Context ClothesAttributeOptionsRepository optionsRepository,
        @Context ClothesAttributeRepository attributesRepository);

    List<ClothesDto> toDto(List<Clothes> clothes,
        @Context FileStorage fileStorage,
        @Context ClothesAttributeDefRepository defsRepository,
        @Context ClothesAttributeOptionsRepository optionsRepository,
        @Context ClothesAttributeRepository attributesRepository);

    @Named("toPublicUrl")
    default String toPublicUrl(String key, @Context FileStorage fileStorage) {
        if (key == null) {
            return null;
        }
        if (fileStorage instanceof S3Storage s3) {
            return s3.generatePresignedUrl(key);
        }
        return key;
    }

    @Named("mapAttributes")
    static List<ClothesAttributeWithDefDto> mapAttributes(
        Clothes clothes,
        @Context ClothesAttributeDefRepository defsRepository,
        @Context ClothesAttributeOptionsRepository optionsRepository,
        @Context ClothesAttributeRepository attributesRepository
    ) {
        List<ClothesAttributes> attrs = attributesRepository.findByClothesId(clothes.getId());

        return attrs.stream().map(attr -> {
            ClothesAttributeDefs def = defsRepository.findById(attr.getDefinition().getId())
                .orElseThrow(() -> new IllegalStateException("의상 속성 정의 없음: " + attr.getDefinition().getId()));

            List<ClothesAttributeOptions> options =
                optionsRepository.findByDefinitionId(attr.getDefinition().getId());

            ClothesAttributeOptions option = optionsRepository.findById(attr.getOption().getId())
                .orElseThrow(() -> new IllegalStateException("의상 속성값 없음: " + attr.getOption().getId()));

            String value = option.getOptionValue();

            return ClothesAttributeWithDefDto.builder()
                .definitionId(def.getId())
                .definitionName(def.getName())
                .selectableValues(options.stream().map(ClothesAttributeOptions::getOptionValue).toList())
                .value(value) // 실제 값 매핑
                .build();
        }).toList();
    }
}
