package com.onepiece.otboo.domain.clothes.mapper;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import org.mapstruct.Mapper;

import com.onepiece.otboo.domain.clothes.entity.Clothes;

@Mapper(componentModel = "spring")
public interface ClothesMapper {

    ClothesDto toDto(Clothes clothes);

}
