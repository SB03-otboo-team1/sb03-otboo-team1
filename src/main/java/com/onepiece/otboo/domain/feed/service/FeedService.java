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
import com.onepiece.otboo.global.event.event.FeedCreatedEvent;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.global.storage.S3Storage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final FileStorage storage;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.cdn-base-url:}")
    private String cdnBaseUrl;

    public FeedResponse create(FeedCreateRequest req) {

        UUID authorId = Objects.requireNonNull(req.authorId(), "authorId must not be null");
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        Profile profile = profileRepository.findByUserId(authorId)
            .orElseThrow(() -> new GlobalException(ErrorCode.PROFILE_NOT_FOUND));

        AuthorDto authorDto = AuthorDto.builder()
            .userId(author.getId())
            .name(profile.getNickname())
            .profileImageUrl(toPublicUrl(profile.getProfileImageUrl()))
            .build();

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

        List<UUID> clothesIds = req.distinctClothesIds();
        List<Clothes> clothesList = clothesRepository.findAllById(clothesIds);
        if (clothesList.size() != clothesIds.size()) {
            throw new GlobalException(ErrorCode.CLOTHES_NOT_FOUND);
        }
        boolean allOwnedByAuthor = clothesList.stream()
            .allMatch(c -> c.getOwner() != null && authorId.equals(c.getOwner().getId()));
        if (!allOwnedByAuthor) {
            throw new GlobalException(ErrorCode.CLOTHES_OWNERSHIP_MISMATCH);
        }

        Feed feed = Feed.builder()
            .authorId(authorId)
            .weatherId(req.weatherId())
            .content(req.content())
            .likeCount(0L)
            .commentCount(0L)
            .build();

        for (Clothes c : clothesList) {
            FeedClothes link = FeedClothes.builder()
                .clothesId(c.getId())
                .build();
            link.setFeed(feed);
            feed.getFeedClothes().add(link);
        }

        Feed saved = feedRepository.save(feed);

        List<OotdDto> ootds = new ArrayList<>();
        for (Clothes c : clothesList) {
            ootds.add(new OotdDto(
                c.getId(),
                c.getName(),
                toPublicUrl(c.getImageUrl()),
                c.getType().name(),
                List.of()
            ));
        }

        boolean likedByMe = false;

        FeedResponse response = feedMapper.toResponse(saved, authorDto, weatherDto, ootds,
            likedByMe);

        eventPublisher.publishEvent(
            new FeedCreatedEvent(response, Instant.now())
        );

        return response;
    }

    @Transactional
    public void delete(UUID feedId, UUID requesterId) {
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new GlobalException(ErrorCode.FEED_NOT_FOUND));

        if (!feed.getAuthorId().equals(requesterId)) {
            throw new GlobalException(ErrorCode.FEED_FORBIDDEN);
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

        // Author
        Profile profile = profileRepository.findByUserId(feed.getAuthorId())
            .orElseThrow(() -> new GlobalException(ErrorCode.PROFILE_NOT_FOUND));
        AuthorDto authorDto = AuthorDto.builder()
            .userId(feed.getAuthorId())
            .name(profile.getNickname())
            .profileImageUrl(toPublicUrl(profile.getProfileImageUrl()))
            .build();

        // Weather (optional)
        WeatherSummaryDto weatherDto = null;
        if (feed.getWeatherId() != null) {
            Weather weather = weatherRepository.findById(feed.getWeatherId())
                .orElseThrow(() -> new GlobalException(ErrorCode.WEATHER_NOT_FOUND));
            weatherDto = new WeatherSummaryDto(
                weather.getId(),
                weather.getSkyStatus(),
                weatherMapper.toPrecipitationDto(weather),
                weatherMapper.toTemperatureDto(weather)
            );
        }

        // OOTDs
        Set<UUID> clothesIds = feed.getFeedClothes().stream()
            .map(FeedClothes::getClothesId)
            .collect(Collectors.toSet());
        List<OotdDto> ootds = clothesRepository.findAllById(clothesIds).stream()
            .map(c -> new OotdDto(
                c.getId(),
                c.getName(),
                toPublicUrl(c.getImageUrl()),
                c.getType().name(),
                List.of()
            ))
            .toList();

        boolean likedByMe = false;

        return feedMapper.toResponse(feed, authorDto, weatherDto, ootds, likedByMe);
    }

    private String toPublicUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }

        if (storage instanceof S3Storage s3) {
            return s3.generatePresignedUrl(key);
        }

        if (cdnBaseUrl != null && !cdnBaseUrl.isBlank()) {
            String left =
                cdnBaseUrl.endsWith("/") ? cdnBaseUrl.substring(0, cdnBaseUrl.length() - 1)
                    : cdnBaseUrl;
            String right = key.startsWith("/") ? key.substring(1) : key;
            return left + "/" + right;
        }
        return key;
    }
}
