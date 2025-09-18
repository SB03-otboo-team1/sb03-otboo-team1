package com.onepiece.otboo.domain.feed.mapper;

import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.dto.response.OotdDto;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.weather.dto.response.WeatherSummaryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FeedMapper {

    @Mapping(target = "author",  ignore = true)
    @Mapping(target = "weather", ignore = true)
    @Mapping(target = "ootds",   expression = "java(java.util.List.of())")
    @Mapping(target = "likedByMe", constant = "false")
    FeedResponse toResponse(Feed feed);


    @Mapping(target = "author",  source = "author")
    @Mapping(target = "weather", source = "weather")
    @Mapping(target = "ootds",   source = "ootds")
    @Mapping(target = "likedByMe", source = "likedByMe")
    FeedResponse toResponse(Feed feed,
                            AuthorDto author,
                            com.onepiece.otboo.domain.weather.dto.response.WeatherSummaryDto weather,
                            List<OotdDto> ootds,
                            boolean likedByMe);

}
