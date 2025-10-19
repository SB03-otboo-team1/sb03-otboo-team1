package com.onepiece.otboo.domain.feed.service;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.repository.ClothesRepository;
import com.onepiece.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.onepiece.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.dto.response.OotdDto;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.feed.entity.FeedClothes;
import com.onepiece.otboo.domain.feed.mapper.FeedMapper;
import com.onepiece.otboo.domain.feed.repository.FeedRepository;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.domain.weather.dto.response.WeatherSummaryDto;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.mapper.WeatherMapper;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedService {

    private final FeedRepository feedRepository;
    private final FeedMapper feedMapper;

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final WeatherRepository weatherRepository;
    private final WeatherMapper weatherMapper;
    private final ClothesRepository clothesRepository;

    public FeedResponse create(FeedCreateRequest req) {
        // 1) 작성자 / 프로필
        UUID authorId = Objects.requireNonNull(req.authorId(), "authorId must not be null");
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        Profile profile = profileRepository.findByUserId(authorId)
            .orElseThrow(() -> new GlobalException(ErrorCode.PROFILE_NOT_FOUND));

        AuthorDto authorDto = AuthorDto.builder()
            .userId(author.getId())
            .name(profile.getNickname())
            .profileImageUrl(profile.getProfileImageUrl())
            .build();

        // 2) 날씨(선택)
        WeatherSummaryDto weatherDto = null;
        if (req.weatherId() != null) {
            Weather weather = weatherRepository.findById(req.weatherId())
                .orElseThrow(() -> new GlobalException(ErrorCode.WEATHER_NOT_FOUND));

            weatherDto = new WeatherSummaryDto(
                weather.getId(),
                weather.getSkyStatus(),
                weatherMapper.toPrecipitationDto(weather),
                weatherMapper.toTemperatureDto(weather)
            );
        }
        // 3) 의상 로딩 + 소유권 검증 + 중복 제거
        List<UUID> clothesIds = req.distinctClothesIds();
        List<Clothes> clothesList = clothesRepository.findAllById(clothesIds);
        if (clothesList.size() != clothesIds.size()) {
            throw new GlobalException(ErrorCode.CLOTHES_NOT_FOUND);
        }
        boolean ownershipMismatch = clothesList.stream()
            .allMatch(c -> c.getOwner() != null && author.getId().equals(c.getOwner().getId()));
        if (ownershipMismatch) {
            throw new GlobalException(ErrorCode.CLOTHES_OWNERSHIP_MISMATCH);
        }

        // 4) Feed 생성 및 FeedClothes 연결 (Cascade로 함께 저장)
        Feed feed = Feed.builder()
            .authorId(authorId)
            .weatherId(req.weatherId())
            .content(req.content())
            .likeCount(0L)
            .commentCount(0L)
            .build();

        Set<UUID> dedup = new HashSet<>(clothesIds);
        for (Clothes c : clothesList) {
            if (dedup.contains(c.getId())) {
                FeedClothes link = FeedClothes.builder()
                    .clothesId(c.getId())
                    .build();
                link.setFeed(feed);
                feed.getFeedClothes().add(link);
            }
        }

        Feed saved = feedRepository.save(feed);

        // 5) OOTD DTO 구성 (미리보기 이미지에 imageUrl 사용)
        List<OotdDto> ootds = new ArrayList<>();
        for (Clothes c : clothesList) {
            ootds.add(new OotdDto(
                c.getId(),
                c.getName(),
                c.getImageUrl(),
                c.getType().name(),
                List.of()
            ));
        }

        // 6) 생성 직후 likedByMe는 항상 false
        boolean likedByMe = false;

        // 7) 모든 필드를 채워 반환
        return feedMapper.toResponse(saved, authorDto, weatherDto, ootds, likedByMe);
    }

    @Transactional
    public void delete(UUID feedId, UUID requesterId) {
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new GlobalException(ErrorCode.FEED_NOT_FOUND)); // 404

        if (!feed.getAuthorId().equals(requesterId)) {
            throw new GlobalException(ErrorCode.FEED_FORBIDDEN); // 403
        }

        feedRepository.delete(feed);
    }

    @Transactional
    public FeedResponse update(UUID feedId, UUID requesterId, FeedUpdateRequest req) {
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new GlobalException(ErrorCode.FEED_NOT_FOUND));
        if (!feed.getAuthorId().equals(requesterId)) {
            throw new GlobalException(ErrorCode.FEED_FORBIDDEN);
        }

        feed.updateContent(req.content());
        return feedMapper.toResponse(feed);
    }

}
