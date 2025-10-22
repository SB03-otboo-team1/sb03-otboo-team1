package com.onepiece.otboo.domain.recommendation.mapper;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeOptions;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributes;
import com.onepiece.otboo.domain.feed.dto.response.OotdDto;
import com.onepiece.otboo.domain.feed.dto.response.OotdDto.OotdAttribute;
import com.onepiece.otboo.domain.recommendation.dto.data.RecommendationDto;
import com.onepiece.otboo.domain.recommendation.entity.Recommendation;
import com.onepiece.otboo.domain.recommendation.entity.RecommendationClothes;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.global.storage.S3Storage;
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mapping(target = "weatherId", source = "recommendation.weather.id")
    @Mapping(target = "userId", source = "recommendation.user.id")
    @Mapping(target = "clothes", source = "clothes")
    RecommendationDto toDto(Recommendation recommendation, List<RecommendationClothes> clothes,
        @Context FileStorage fileStorage);

    @Mapping(target = "clothesId", source = "clothes.clothes.id")
    @Mapping(target = "type", source = "clothes.clothes.type")
    @Mapping(target = "name", source = "clothes.clothes.name")
    @Mapping(target = "imageUrl", source = "clothes.clothes.imageUrl", qualifiedByName = "toPublicUrl")
    @Mapping(target = "attributes", source = "clothes.clothes.attributes")
    OotdDto clothesToOotdDto(RecommendationClothes clothes,
        @Context FileStorage fileStorage);

    @Mapping(target = "definitionId", source = "attribute.definition.id")
    @Mapping(target = "definitionName", source = "attribute.definition.name")
    @Mapping(target = "selectableValues", source = "attribute.definition.options", qualifiedByName = "optionsToValues")
    @Mapping(target = "value", source = "attribute.optionValue")
    OotdAttribute toOotdAttribute(ClothesAttributes attribute);

    List<OotdAttribute> toOotdAttribute(List<ClothesAttributes> attribute);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recommendation", source = "recommendation")
    @Mapping(target = "clothes", source = "clothes")
    RecommendationClothes toRecommendationClothes(Recommendation recommendation, Clothes clothes);

    @Named("toPublicUrl")
    default String toPublicUrl(String key, @Context FileStorage fileStorage) {
        if (key == null) {
            return null;
        }
        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }
        if (fileStorage instanceof S3Storage s3) {
            return s3.generatePresignedUrl(key);
        }
        return key;
    }

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
