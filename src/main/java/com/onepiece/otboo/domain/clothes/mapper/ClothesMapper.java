package com.onepiece.otboo.domain.clothes.mapper;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.List;
import org.mapstruct.Mapper;

import com.onepiece.otboo.domain.clothes.entity.Clothes;

@Mapper(componentModel = "spring")
public interface ClothesMapper {

    ClothesDto toDto(Clothes clothes);

}
