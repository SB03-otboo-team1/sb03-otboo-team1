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
import org.springframework.beans.factory.annotation.Value;
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
            .profileImageUrl(resolveImageUrl(profile.getProfileImageUrl()))
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

        List<OotdDto> ootds = new ArrayList<>();
        for (Clothes c : clothesList) {
            ootds.add(new OotdDto(
                c.getId(),
                c.getName(),
                resolveImageUrl(c.getImageUrl()),
                c.getType().name(),
                List.of()
            ));
        }

        boolean likedByMe = false;

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

    private String resolveImageUrl(String raw) {
        if (raw == null || raw.isBlank()) return null;
        if (raw.startsWith("http://") || raw.startsWith("https://")) return raw;
        String base = (cdnBaseUrl == null || cdnBaseUrl.isBlank())
            ? "" : (cdnBaseUrl.endsWith("/") ? cdnBaseUrl.substring(0, cdnBaseUrl.length()-1) : cdnBaseUrl);
        String path = raw.startsWith("/") ? raw : "/" + raw;
        return base.isEmpty() ? path : base + path;
    }
}
