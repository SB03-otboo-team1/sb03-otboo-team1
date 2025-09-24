package com.onepiece.otboo.domain.profile.mapper;

import com.onepiece.otboo.domain.profile.dto.response.ProfileDto;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "name", source = "profile.nickname")
    @Mapping(target = "gender", source = "profile.gender")
    @Mapping(target = "birthDate", source = "profile.birthDate")
    @Mapping(target = "location", expression = "java(toWeatherAPILocation(profile))")
    @Mapping(target = "temperatureSensitivity", source = "profile.tempSensitivity")
    @Mapping(target = "profileImageUrl", source = "profile.profileImageUrl")
    ProfileDto toDto(User user, Profile profile);

    // Location → WeatherAPILocation 변환
    default WeatherAPILocation toWeatherAPILocation(Profile profile) {
        if (profile.getLocation() == null) {
            return null;
        }
        return WeatherAPILocation.toDto(profile.getLocation());
    }
}
