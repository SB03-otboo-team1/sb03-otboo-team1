package com.onepiece.otboo.domain.clothes.mapper;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeOptions;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeOptionsRepository;
import java.util.List;
import java.util.Map;
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

    @Mapping(target = "definitionId", source = "attr", qualifiedByName = "toDefId")
    @Mapping(target = "definitionName", source = "attr")
    @Mapping(target = "selectableValues", source = "attr", qualifiedByName = "toOptions")
    @Mapping(target = "value", source = "dto.value")
    ClothesAttributeWithDefDto toAttributeDto(Map<String, String> attr,
        @Context ClothesAttributeDefRepository def,
        @Context ClothesAttributeOptionsRepository option);

    @Named("toDefId")
    default UUID toDefId(String name, @Context ClothesAttributeDefRepository def) {
        UUID defId = def.findByName(name).map(ClothesAttributeDefs::getId).orElse(null);

        return defId;
    }

    @Named("toOptions")
    default List<ClothesAttributeOptions> toOptions(String name,
        @Context ClothesAttributeDefRepository def,
        @Context ClothesAttributeOptionsRepository option) {
        UUID defId = def.findByName(name).map(ClothesAttributeDefs::getId).orElse(null);
        List<ClothesAttributeOptions> options = option.findByDefinitionId(defId);

        return options;
    }
}
