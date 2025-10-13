package com.onepiece.otboo.domain.recommendation.service;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.repository.ClothesRepository;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.recommendation.dto.data.RecommendationDto;
import com.onepiece.otboo.domain.recommendation.entity.Recommendation;
import com.onepiece.otboo.domain.recommendation.entity.RecommendationClothes;
import com.onepiece.otboo.domain.recommendation.mapper.RecommendationMapper;
import com.onepiece.otboo.domain.recommendation.repository.RecommendationClothesRepository;
import com.onepiece.otboo.domain.recommendation.repository.RecommendationRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import com.onepiece.otboo.global.storage.FileStorage;
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
    //    private final RecommendationParameterRepository parameterRepository;
    private final WeatherRepository weatherRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ClothesRepository clothesRepository;
    //    private final ClothesAttributeRepository attributeRepository;
//    private final ClothesAttributeDefRepository defRepository;
//    private final ClothesAttributeOptionsRepository optionRepository;
    private final RecommendationMapper recommendationMapper;

    private final FileStorage fileStorage;

    @Override
    @Transactional(readOnly = true)
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

        List<RecommendationClothes> recommendationClothesList = getRecommendationClothes(
            recommendation);

        return recommendationMapper.toDto(recommendation, recommendationClothesList, fileStorage);
    }

    public List<RecommendationClothes> getRecommendationClothes(Recommendation recommendation) {
//        Weather weather = recommendation.getWeather();
//        Profile profile = recommendation.getProfile();
        User user = recommendation.getProfile().getUser();

        log.debug("의상 추천 알고리즘 작업 시작");
        List<Clothes> clothesList = new ArrayList<>();

        // 기본 랜덤 추천
        for (ClothesType type : ClothesType.values()) {
            int size = clothesRepository.countClothesByOwnerIdAndType(user.getId(), type);
            if (size != 0) {
                clothesList.add(
                    clothesRepository.getClothesByOwnerIdAndType(user.getId(), type)
                        .get(random.nextInt(size))
                );
            }
        }

//        // 의상 속성 활용하기
//        RecommendationParameter parameter = includeAttribute(recommendation);
//
        // 상하의/원피스로 구분
        clothesList = includeDress(user.getId());

        List<RecommendationClothes> recommendationClothesList = new ArrayList<>();

        if (!clothesList.isEmpty()) {
            log.debug("추천 객체 저장 - recommendationId: {}", recommendation.getId());
            Recommendation savedRecommendation = recommendationRepository.save(recommendation);

            log.debug("추천 객체 저장 완료 - savedRecommendationId: {}", savedRecommendation.getId());

            recommendationClothesList = clothesList.stream().map(
                c -> {
                    return recommendationMapper.toRecommendationClothes(savedRecommendation, c);
                }
            ).toList();

//            log.debug("추천 관련 파라미터 저장 - recommendationId: {}", savedRecommendation.getId());
//            parameterRepository.save(parameter);

            log.debug("추천 의상 저장 - recommendationId: {}", savedRecommendation.getId());
            recommendationClothesRepository.saveAll(recommendationClothesList);

            log.debug("추천 작업 관련 객체 저장 완료 - recommendationId: {}", savedRecommendation.getId());

        }

        return recommendationClothesList;
    }

    public List<Clothes> includeDress(UUID userId) {

        List<Clothes> clothesList = new ArrayList<>();

        // 상하의 / 원피스 케이스 나누기
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
}
