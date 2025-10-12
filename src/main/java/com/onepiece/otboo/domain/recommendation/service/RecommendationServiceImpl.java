package com.onepiece.otboo.domain.recommendation.service;

import com.onepiece.otboo.domain.clothes.repository.ClothesRepository;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.enums.Gender;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.recommendation.dto.data.RecommendationDto;
import com.onepiece.otboo.domain.recommendation.mapper.RecommendationMapper;
import com.onepiece.otboo.domain.recommendation.repository.RecommendationRepository;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final WeatherRepository weatherRepository;
    private final ProfileRepository profileRepository;
    private final ClothesRepository clothesRepository;
    private final RecommendationMapper recommendationMapper;

    @Override
    public List<RecommendationDto> getRecommendations(UUID weatherId, UUID userId) {

        Weather weather = weatherRepository.findById(weatherId).orElseThrow();

        SkyStatus skyStatus = weather.getSkyStatus();
        Double humidity = weather.getHumidity();
        Double windSpeed = weather.getWindSpeed();
        Double maxTemp = weather.getTemperatureMax();
        Double minTemp = weather.getTemperatureMin();
        Double curTemp = weather.getTemperatureCurrent();

        Profile profile = profileRepository.findByUserId(userId).orElseThrow(
            () -> UserNotFoundException.byId(userId)
        );

        Gender gender = profile.getGender();
        Integer tempSens = profile.getTempSensitivity();
        LocalDate birthDate = profile.getBirthDate();

        return null;
    }

}
