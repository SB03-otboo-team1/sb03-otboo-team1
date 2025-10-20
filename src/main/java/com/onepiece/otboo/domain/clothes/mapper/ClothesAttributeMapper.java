package com.onepiece.otboo.domain.clothes.mapper;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeOptions;
import com.onepiece.otboo.domain.clothes.exception.ClothesAttributeDefNotFoundException;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeOptionsRepository;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ClothesAttributeMapper {

    @Mapping(target = "definitionId", source = "def.id")
    @Mapping(target = "definitionName", source = "def.name")
    @Mapping(target = "selectableValues", source = "def.options", qualifiedByName = "optionsToValues")
    @Mapping(target = "value", source = "value")
    ClothesAttributeWithDefDto toAttributeWithDefDto(
        ClothesAttributeDefs def,
        String value
    );

    @Mapping(target = "id", source = "def.id")
    @Mapping(target = "name", source = "def.name")
    @Mapping(target = "selectableValues", source = "def.options", qualifiedByName = "optionsToValues")
    ClothesAttributeDefDto toAttributeDefDto(ClothesAttributeDefs def);

    List<ClothesAttributeDefDto> toAttributeDefDto(List<ClothesAttributeDefs> defs);

    @Named("optionsToValues")
    default List<String> optionsToValues(List<ClothesAttributeOptions> options) {
        if (options == null) {
            return List.of();
        }
        return options.stream()
            .map(ClothesAttributeOptions::getOptionValue)
            .toList();
    }

    @Mapping(target = "definitionId", source = "attrName", qualifiedByName = "toDefId")
    @Mapping(target = "definitionName", source = "attrName")
    @Mapping(target = "selectableValues", source = "attrName", qualifiedByName = "toOptions")
    @Mapping(target = "value", source = "attrValue")
    ClothesAttributeWithDefDto toAttributeDto(String attrName, String attrValue,
        @Context ClothesAttributeDefRepository def,
        @Context ClothesAttributeOptionsRepository option);

    @Named("toDefId")
    default UUID toDefId(String name, @Context ClothesAttributeDefRepository def) {
        UUID defId = def.findByName(name).orElseThrow(
            () -> new ClothesAttributeDefNotFoundException("해당 의상 속성 정보를 찾을 수 없습니다. name: " + name)
        ).getId();

        return defId;
    }

    @Named("toOptions")
    default List<String> toOptions(String attrName,
        @Context ClothesAttributeDefRepository def,
        @Context ClothesAttributeOptionsRepository option) {

        UUID defId = def.findByName(attrName).map(ClothesAttributeDefs::getId).orElse(null);
        if (defId == null) {
            return List.of();
        }
        List<ClothesAttributeOptions> options = option.findByDefinitionId(defId);
        List<String> selectableValues = options.stream()
            .map(ClothesAttributeOptions::getOptionValue).toList();

        return selectableValues;
    }
}
