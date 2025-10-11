package com.onepiece.otboo.domain.recommendation.mapper;

import com.onepiece.otboo.domain.feed.dto.response.OotdDto;
import com.onepiece.otboo.domain.recommendation.dto.data.RecommendationDto;
import com.onepiece.otboo.domain.recommendation.entity.Recommendation;
import com.onepiece.otboo.domain.recommendation.entity.RecommendationClothes;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mapping(target = "weatherId", source = "weather.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "clothes", source = "clothes")
    RecommendationDto toDto(Recommendation recommendation, List<RecommendationClothes> clothes);

    @Mapping(target = "clothedId", source = "clothes.clothes.id")
    @Mapping(target = "type", source = "clothes.clothes.type")
    @Mapping(target = "name", source = "clothes.clothes.name")
    @Mapping(target = "imageUrl", source = "clothes.clothes.imageUrl")
    OotdDto clothesToOotdDto(RecommendationClothes clothes);
}
