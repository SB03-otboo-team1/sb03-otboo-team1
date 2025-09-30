package com.onepiece.otboo.domain.clothes.mapper;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeOptions;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributes;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ClothesAttributeMapper {

    @Mapping(target = "definitionId", source = "def.id")
    @Mapping(target = "attributeId", source = "attribute.id")
    @Mapping(target = "selectableValues", source = "options", qualifiedByName = "optionsToValues")
    @Mapping(target = "value", source = "attribute.value")
    ClothesAttributeWithDefDto toAttributeWithDefDto(
        ClothesAttributeDefs def,
        ClothesAttributes attribute,
        List<ClothesAttributeOptions> options
    );

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
