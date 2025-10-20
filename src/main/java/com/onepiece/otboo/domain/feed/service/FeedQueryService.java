package com.onepiece.otboo.domain.feed.service;

import static com.onepiece.otboo.domain.clothes.entity.QClothes.clothes;
import static com.onepiece.otboo.domain.feed.entity.QFeed.feed;
import static com.onepiece.otboo.domain.feed.entity.QFeedClothes.feedClothes;
import static com.onepiece.otboo.domain.profile.entity.QProfile.profile;
import static com.onepiece.otboo.domain.weather.entity.QWeather.weather;

import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.dto.response.OotdDto;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.feed.mapper.FeedMapper;
import com.onepiece.otboo.domain.feed.repository.FeedLikeRepository;
import com.onepiece.otboo.domain.weather.dto.response.PrecipitationDto;
import com.onepiece.otboo.domain.weather.dto.response.TemperatureDto;
import com.onepiece.otboo.domain.weather.dto.response.WeatherSummaryDto;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.enums.PrecipitationType;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import com.onepiece.otboo.domain.weather.mapper.WeatherMapper;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.global.storage.S3Storage;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedQueryService {

    private final JPAQueryFactory qf;
    private final FeedMapper feedMapper;
    private final FeedLikeRepository feedLikeRepository;
    private final WeatherMapper weatherMapper;
    private final FileStorage storage; // ★ 추가

    @Value("${app.cdn-base-url:}")
    private String cdnBaseUrl;

    @Transactional(readOnly = true)
    public CursorPageResponseDto<FeedResponse> listFeeds(
        @Nullable String cursor,
        @Nullable UUID idAfter,
        int limit,
        SortBy sortBy,
        SortDirection sortDirection,
        @Nullable String keywordLike,
        @Nullable String skyStatusEqual,
        @Nullable String precipitationTypeEqual,
        @Nullable UUID authorIdEqual,
        @Nullable UUID me
    ) {
        if (limit <= 0 || limit > 100) throw new IllegalArgumentException("limit must be between 1 and 100");

        final SortBy sb = sortBy;
        final SortDirection sd = sortDirection;

        BooleanBuilder where = new BooleanBuilder();
        if (authorIdEqual != null) where.and(feed.authorId.eq(authorIdEqual));
        if (keywordLike != null && !keywordLike.isBlank()) where.and(feed.content.containsIgnoreCase(keywordLike));

        boolean joinWeather = false;

        if (skyStatusEqual != null && !skyStatusEqual.isBlank()) {
            joinWeather = true;
            where.and(weather.skyStatus.eq(SkyStatus.valueOf(skyStatusEqual.toUpperCase())));
        }
        if (precipitationTypeEqual != null && !precipitationTypeEqual.isBlank()) {
            joinWeather = true;
            where.and(weather.precipitationType.eq(PrecipitationType.valueOf(precipitationTypeEqual.toUpperCase())));
        }

        if (cursor != null && idAfter != null) {
            switch (sb) {
                case CREATED_AT -> {
                    final Instant key;
                    try { key = Instant.parse(cursor); }
                    catch (Exception e) { throw new IllegalArgumentException("Invalid cursor for createdAt: " + cursor); }
                    if (sd == SortDirection.ASCENDING) {
                        where.and(feed.createdAt.gt(key)
                            .or(feed.createdAt.eq(key).and(feed.id.gt(idAfter))));
                    } else {
                        where.and(feed.createdAt.lt(key)
                            .or(feed.createdAt.eq(key).and(feed.id.lt(idAfter))));
                    }
                }
                case LIKE_COUNT -> {
                    final long key;
                    try { key = Long.parseLong(cursor); }
                    catch (Exception e) { throw new IllegalArgumentException("Invalid cursor for likeCount: " + cursor); }
                    if (sd == SortDirection.ASCENDING) {
                        where.and(feed.likeCount.gt(key)
                            .or(feed.likeCount.eq(key).and(feed.id.gt(idAfter))));
                    } else {
                        where.and(feed.likeCount.lt(key)
                            .or(feed.likeCount.eq(key).and(feed.id.lt(idAfter))));
                    }
                }
            }
        }

        OrderSpecifier<?> primary = (sb == SortBy.CREATED_AT)
            ? (sd == SortDirection.ASCENDING ? feed.createdAt.asc() : feed.createdAt.desc())
            : (sd == SortDirection.ASCENDING ? feed.likeCount.asc() : feed.likeCount.desc());
        OrderSpecifier<?> tie = (sd == SortDirection.ASCENDING ? feed.id.asc() : feed.id.desc());

        var jq = qf.selectFrom(feed);
        if (joinWeather) jq.leftJoin(weather).on(weather.id.eq(feed.weatherId));
        List<Feed> rows = jq.where(where).orderBy(primary, tie).limit(limit + 1L).fetch();

        boolean hasNext = rows.size() > limit;
        if (hasNext) rows = new ArrayList<>(rows.subList(0, limit));

        List<UUID> feedIds = rows.stream().map(Feed::getId).toList();

        Set<UUID> likedIds = (me != null && !feedIds.isEmpty())
            ? feedLikeRepository.findAllByUser_IdAndFeed_IdIn(me, feedIds).stream()
            .map(fl -> fl.getFeed().getId())
            .collect(Collectors.toSet())
            : Set.of();

        // ---- Authors ----
        List<UUID> authorIds = rows.stream().map(Feed::getAuthorId).distinct().toList();
        Map<UUID, AuthorDto> authorMap = new HashMap<>();
        if (!authorIds.isEmpty()) {
            List<Tuple> authorTuples = qf
                .select(
                    profile.user.id,
                    profile.nickname,
                    profile.profileImageUrl
                )
                .from(profile)
                .where(profile.user.id.in(authorIds))
                .fetch();

            for (Tuple t : authorTuples) {
                UUID uid = t.get(profile.user.id);
                String nickname = t.get(profile.nickname);
                String img = t.get(profile.profileImageUrl);

                authorMap.put(uid, AuthorDto.builder()
                    .userId(uid)
                    .name(nickname)
                    .profileImageUrl(toPublicUrl(img))
                    .build());
            }
        }

        // ---- Weather ----
        List<UUID> weatherIds = rows.stream().map(Feed::getWeatherId).filter(Objects::nonNull).distinct().toList();
        Map<UUID, WeatherSummaryDto> weatherMap = new HashMap<>();
        if (!weatherIds.isEmpty()) {
            List<Weather> ws = qf.selectFrom(weather).where(weather.id.in(weatherIds)).fetch();
            for (Weather w : ws) {
                weatherMap.put(
                    w.getId(),
                    new WeatherSummaryDto(
                        w.getId(),
                        w.getSkyStatus(),
                        weatherMapper.toPrecipitationDto(w),
                        weatherMapper.toTemperatureDto(w)
                    )
                );
            }
        }

        // ---- OOTDs ----
        Map<UUID, List<OotdDto>> ootdMap = new HashMap<>();
        if (!feedIds.isEmpty()) {
            List<Tuple> ootdTuples = qf
                .select(feedClothes.feed.id, clothes.id, clothes.name, clothes.imageUrl, clothes.type)
                .from(feedClothes)
                .join(clothes).on(clothes.id.eq(feedClothes.clothesId))
                .where(feedClothes.feed.id.in(feedIds))
                .fetch();

            for (Tuple t : ootdTuples) {
                UUID fId = t.get(feedClothes.feed.id);
                UUID cId = t.get(clothes.id);
                String cName = t.get(clothes.name);
                String imgRaw = t.get(clothes.imageUrl);
                String typeStr = t.get(clothes.type).name();

                ootdMap.computeIfAbsent(fId, k -> new ArrayList<>())
                    .add(new OotdDto(cId, cName, toPublicUrl(imgRaw), typeStr, List.of()));
            }
        }

        // ---- Assemble ----
        List<FeedResponse> data = rows.stream()
            .map(f -> feedMapper.toResponse(
                f,
                authorMap.getOrDefault(f.getAuthorId(), new AuthorDto(null, "", null)),
                (f.getWeatherId() != null) ? weatherMap.get(f.getWeatherId()) : null,
                ootdMap.getOrDefault(f.getId(), List.of()),
                likedIds.contains(f.getId())
            ))
            .map(this::ensureDefaults)
            .toList();

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (hasNext && !rows.isEmpty()) {
            Feed last = rows.get(rows.size() - 1);
            nextCursor = (sb == SortBy.CREATED_AT) ? last.getCreatedAt().toString() : String.valueOf(last.getLikeCount());
            nextIdAfter = last.getId();
        }

        long totalCount = countAllWithoutCursor(keywordLike, skyStatusEqual, precipitationTypeEqual, authorIdEqual, joinWeather);
        return new CursorPageResponseDto<>(data, nextCursor, nextIdAfter, hasNext, totalCount, sb, sd);
    }

    private long countAllWithoutCursor(
        @Nullable String keywordLike,
        @Nullable String skyStatusEqual,
        @Nullable String precipitationTypeEqual,
        @Nullable UUID authorIdEqual,
        boolean joinWeather
    ) {
        BooleanBuilder where = new BooleanBuilder();
        if (authorIdEqual != null) where.and(feed.authorId.eq(authorIdEqual));
        if (keywordLike != null && !keywordLike.isBlank()) where.and(feed.content.containsIgnoreCase(keywordLike));
        if (skyStatusEqual != null && !skyStatusEqual.isBlank()) where.and(weather.skyStatus.eq(SkyStatus.valueOf(skyStatusEqual.toUpperCase())));
        if (precipitationTypeEqual != null && !precipitationTypeEqual.isBlank()) where.and(weather.precipitationType.eq(PrecipitationType.valueOf(precipitationTypeEqual.toUpperCase())));

        var q = qf.select(feed.count()).from(feed);
        if (joinWeather) q.leftJoin(weather).on(weather.id.eq(feed.weatherId));
        Long cnt = q.where(where).fetchOne();
        return (cnt == null) ? 0L : cnt;
    }

    private FeedResponse ensureDefaults(FeedResponse r) {
        AuthorDto author = (r.author() != null) ? r.author() : new AuthorDto(null, "", null);

        WeatherSummaryDto weatherDto = r.weather();
        if (weatherDto == null) {
            weatherDto = new WeatherSummaryDto(
                null,
                SkyStatus.CLEAR,
                new PrecipitationDto(PrecipitationType.NONE, 0.0, 0.0),
                new TemperatureDto(null, null, null, null)
            );
        }

        List<OotdDto> ootds = (r.ootds() != null) ? r.ootds() : List.of();

        return new FeedResponse(
            r.id(),
            r.createdAt(),
            r.updatedAt(),
            author,
            weatherDto,
            ootds,
            r.content(),
            r.likeCount(),
            r.commentCount(),
            r.likedByMe()
        );
    }

    private String toPublicUrl(String key) {
        if (key == null || key.isBlank()) return null;
        if (key.startsWith("http://") || key.startsWith("https://")) return key;

        // S3 구현이면 presigned URL
        if (storage instanceof S3Storage s3) {
            return s3.generatePresignedUrl(key);
        }

        if (cdnBaseUrl != null && !cdnBaseUrl.isBlank()) {
            String left  = cdnBaseUrl.endsWith("/") ? cdnBaseUrl.substring(0, cdnBaseUrl.length() - 1) : cdnBaseUrl;
            String right = key.startsWith("/") ? key.substring(1) : key;
            return left + "/" + right;
        }
        return key;
    }
}
