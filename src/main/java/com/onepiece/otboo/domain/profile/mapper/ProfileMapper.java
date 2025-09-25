package com.onepiece.otboo.domain.profile.mapper;

import com.onepiece.otboo.domain.profile.dto.response.ProfileDto;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.weather.dto.data.WeatherAPILocation;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.global.storage.S3Storage;
import org.mapstruct.Context;
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
    @Mapping(target = "profileImageUrl",
        expression = "java(toPublicUrl(profile.getProfileImageUrl(), storage))")
    ProfileDto toDto(User user, Profile profile, @Context FileStorage storage);

    // Location → WeatherAPILocation 변환
    default WeatherAPILocation toWeatherAPILocation(Profile profile) {
        if (profile.getLocation() == null) {
            return null;
        }
        return WeatherAPILocation.toDto(profile.getLocation());
    }

    default String toPublicUrl(String key, @Context FileStorage storage) {
        if (key == null) return null;
        if (storage instanceof S3Storage s3) {
            return s3.generatePresignedUrl(key);
        }
        return key;
    }
}
