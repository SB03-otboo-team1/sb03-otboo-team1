package com.onepiece.otboo.domain.clothes.mapper;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.global.storage.S3Storage;
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {ClothesAttributeMapper.class})
public interface ClothesMapper {

    @Mapping(target = "imageUrl",
        source = "clothes.imageUrl",
        qualifiedByName = "toPublicUrl")
    @Mapping(target = "attributes", ignore = true)
    ClothesDto toDto(Clothes clothes, @Context FileStorage fileStorage);

    List<ClothesDto> toDto(List<Clothes> clothes, @Context FileStorage fileStorage);


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
}
