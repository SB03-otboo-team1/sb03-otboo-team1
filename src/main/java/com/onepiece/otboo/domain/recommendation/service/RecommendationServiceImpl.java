package com.onepiece.otboo.domain.recommendation.service;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeOptionsRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeRepository;
import com.onepiece.otboo.domain.clothes.repository.ClothesRepository;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.enums.Gender;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.recommendation.dto.data.RecommendationDto;
import com.onepiece.otboo.domain.recommendation.entity.Recommendation;
import com.onepiece.otboo.domain.recommendation.entity.RecommendationClothes;
import com.onepiece.otboo.domain.recommendation.entity.RecommendationParameter;
import com.onepiece.otboo.domain.recommendation.mapper.RecommendationMapper;
import com.onepiece.otboo.domain.recommendation.repository.RecommendationClothesRepository;
import com.onepiece.otboo.domain.recommendation.repository.RecommendationParameterRepository;
import com.onepiece.otboo.domain.recommendation.repository.RecommendationRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import com.onepiece.otboo.global.storage.FileStorage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final Random random = new Random();

    private final RecommendationRepository recommendationRepository;
    private final RecommendationClothesRepository recommendationClothesRepository;
    private final RecommendationParameterRepository parameterRepository;
    private final WeatherRepository weatherRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ClothesRepository clothesRepository;
    private final ClothesAttributeRepository attributeRepository;
    private final ClothesAttributeDefRepository defRepository;
    private final ClothesAttributeOptionsRepository optionRepository;
    private final RecommendationMapper recommendationMapper;

    private final FileStorage fileStorage;

    @Override
    public RecommendationDto getRecommendation(UUID weatherId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> UserNotFoundException.byEmail(email));
        UUID userId = user.getId();

        log.info("[추천 조회] 작업 시작 - weatherId: {}, userId: {}", weatherId, userId);

        Weather weather = weatherRepository.findById(weatherId).orElseThrow();

        Profile profile = profileRepository.findByUserId(userId).orElseThrow(
            () -> UserNotFoundException.byId(userId)
        );

        log.debug("추천 객체 생성 - weather: {}, user: {}", weatherId, userId);
        Recommendation recommendation =
            Recommendation.builder()
                .weather(weather)
                .profile(profile)
                .build();

        log.debug("추천 객체 저장 - recommendationId: {}", recommendation.getId());
        Recommendation savedRecommendation = recommendationRepository.save(recommendation);

        log.debug("추천 객체 저장 완료 - savedRecommendationId: {}", savedRecommendation.getId());

        List<RecommendationClothes> recommendationClothesList = getRecommendationClothes(
            savedRecommendation);

        return recommendationMapper.toDto(savedRecommendation, recommendationClothesList,
            fileStorage);
    }

    public List<RecommendationClothes> getRecommendationClothes(Recommendation recommendation) {
        Weather weather = recommendation.getWeather();
        Profile profile = recommendation.getProfile();
        UUID userId = profile.getUser().getId();

        log.debug("의상 추천 알고리즘 작업 시작");
        List<Clothes> clothesList = new ArrayList<>();

        // 기본 랜덤 추천
        for (ClothesType type : ClothesType.values()) {
            int size = clothesRepository.countClothesByOwnerIdAndType(userId, type);
            if (size != 0) {
                clothesList.add(
                    clothesRepository.getClothesByOwnerIdAndType(userId, type)
                        .get(random.nextInt(size))
                );
            }
        }

        // 상하의/원피스로 구분
        clothesList = recommendClothes(recommendation);

        List<RecommendationClothes> recommendationClothesList = clothesList.stream().map(
            c -> {
                return recommendationMapper.toRecommendationClothes(recommendation, c);
            }
        ).toList();

        log.debug("추천 의상 저장 - recommendationId: {}", recommendation.getId());

        return recommendationClothesRepository.saveAll(recommendationClothesList);
    }

    public List<Clothes> recommendClothes(Recommendation recommendation) {

        List<Clothes> clothesList = new ArrayList<>();

        // 계절, 날씨, 사용자 데이터 활용하기
        RecommendationParameter parameter = extractData(recommendation);

        double maxTemp = parameter.getMaxTemp();
        double curTemp = parameter.getCurTemp();

        // 아우터 제외
        boolean excludeOuter = maxTemp >= 30 || curTemp >= 27;

        // 상하의 / 원피스 케이스 나누기
        UUID userId = recommendation.getProfile().getUser().getId();

        boolean hasDress =
            clothesRepository.countClothesByOwnerIdAndType(userId, ClothesType.DRESS) > 0;

        long totalFeedCount = clothesRepository.getClothesByOwnerId(userId)
            .stream().map(c -> c.getFeedCount()).reduce(0L, Long::sum);

        long dressFeedCount = clothesRepository.getClothesByOwnerIdAndType(userId,
                ClothesType.DRESS)
            .stream().map(c -> c.getFeedCount()).reduce(0L, Long::sum);

        double feedDress = (double) dressFeedCount / (double) totalFeedCount;

        if (totalFeedCount == 0) {
            feedDress = 0.0;
        }

        boolean chooseDress = hasDress && random.nextDouble() < (0.2 + feedDress * 2);

        if (chooseDress) {
            for (ClothesType type : ClothesType.values()) {
                if (type == ClothesType.TOP || type == ClothesType.BOTTOM) {
                    continue;
                }
                if (excludeOuter && type == ClothesType.OUTER) {
                    continue;
                }
                int size = clothesRepository.countClothesByOwnerIdAndType(userId, type);
                if (size != 0) {
                    clothesList.add(
                        clothesRepository.getClothesByOwnerIdAndType(userId, type)
                            .get(random.nextInt(size))
                    );
                }
            }
        } else {
            for (ClothesType type : ClothesType.values()) {
                if (type == ClothesType.DRESS) {
                    continue;
                }
                if (excludeOuter && type == ClothesType.OUTER) {
                    continue;
                }
                int size = clothesRepository.countClothesByOwnerIdAndType(userId, type);
                if (size != 0) {
                    clothesList.add(
                        clothesRepository.getClothesByOwnerIdAndType(userId, type)
                            .get(random.nextInt(size))
                    );
                }
            }
        }
        return clothesList;
    }

    public RecommendationParameter extractData(Recommendation recommendation) {

        Weather weather = recommendation.getWeather();
        Profile profile = recommendation.getProfile();

        // 계절 파라미터
        LocalDate now = LocalDate.now();
        Integer seasonInt =
            switch (now.toString().substring(5, 7)) {
                case "03", "04", "05" -> 1; // 봄
                case "06", "07", "08" -> 2; // 여름
                case "09", "10", "11" -> 3; // 가을
                case "12", "01", "02" -> 4; // 겨울
                default -> null;
            };

        // 날씨 파라미터
        SkyStatus skyStatus = weather.getSkyStatus();
        Integer skyStatusInt =
            skyStatus == SkyStatus.CLEAR ? 2 : skyStatus == SkyStatus.CLOUDY ? 0 : 1;
        Double humidity = weather.getHumidity();
        Double windSpeed = weather.getWindSpeed();
        Double maxTemp = weather.getTemperatureMax();
        Double minTemp = weather.getTemperatureMin();
        Double curTemp = weather.getTemperatureCurrent();

        // 사용자 파라미터
        Gender gender = null;
        Integer genderInt = null;
        Integer tempSens = null;
        LocalDate birthDate = null;
        Integer age = null;

        gender = profile.getGender();
        if (gender != null) {
            genderInt = gender == Gender.FEMALE ? 0 : 1;
        }
        tempSens = profile.getTempSensitivity();
        birthDate = profile.getBirthDate();
        if (birthDate != null) {
            age =
                LocalDate.now().getYear() - Integer.valueOf(birthDate.toString().substring(0, 4))
                    + 1;
        }

        // 체감온도 가중치
        Double feelHot = 0.0;
        Double feelCold = 0.0;

        log.info("추천 관련 파라미터 객체 생성");
        RecommendationParameter parameter =
            RecommendationParameter.builder()
                .recommendation(recommendation)
                .age(age)
                .genderInt(genderInt)
                .tempSens(tempSens)
                .feelHot(feelHot)
                .feelCold(feelCold)
                .seasonInt(seasonInt)
                .skyStatusInt(skyStatusInt)
                .curTemp(curTemp)
                .minTemp(minTemp)
                .maxTemp(maxTemp)
                .windSpeed(windSpeed)
                .humidity(humidity)
                .build();

        log.debug("추천 관련 파라미터 저장 - recommendationId: {}", recommendation.getId());
        return parameterRepository.save(parameter);
    }
}
