package com.onepiece.otboo.domain.clothes.mapper;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeOptions;
import java.util.List;
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
        List<ClothesAttributeOptions> options,
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
}
