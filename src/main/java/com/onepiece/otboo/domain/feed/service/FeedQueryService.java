package com.onepiece.otboo.domain.feed.service;

import static com.onepiece.otboo.domain.feed.entity.QFeed.feed;
import static com.onepiece.otboo.domain.weather.entity.QWeather.weather;

import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.dto.response.OotdDto;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.feed.mapper.FeedMapper;
import com.onepiece.otboo.domain.weather.dto.response.PrecipitationDto;
import com.onepiece.otboo.domain.weather.dto.response.TemperatureDto;
import com.onepiece.otboo.domain.weather.dto.response.WeatherSummaryDto;
import com.onepiece.otboo.domain.weather.enums.PrecipitationType;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedQueryService {

    private final JPAQueryFactory qf;
    private final FeedMapper feedMapper;

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
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("limit must be between 1 and 100");
        }

        final SortBy sb;
        final SortDirection sd;
        try {
            sb = sortBy;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sortBy: " + sortBy);
        }
        try {
            sd = sortDirection;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sortDirection: " + sortDirection);
        }

        BooleanBuilder where = new BooleanBuilder();
        if (authorIdEqual != null) {
            where.and(feed.authorId.eq(authorIdEqual));
        }
        if (keywordLike != null && !keywordLike.isBlank()) {
            where.and(feed.content.containsIgnoreCase(keywordLike));
        }

        boolean joinWeather = false;

        if (skyStatusEqual != null && !skyStatusEqual.isBlank()) {
            joinWeather = true;
            final SkyStatus ss;
            try {
                ss = SkyStatus.valueOf(skyStatusEqual.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid skyStatusEqual: " + skyStatusEqual);
            }
            where.and(weather.skyStatus.eq(ss));
        }

        if (precipitationTypeEqual != null && !precipitationTypeEqual.isBlank()) {
            joinWeather = true;
            final PrecipitationType pt;
            try {
                pt = PrecipitationType.valueOf(precipitationTypeEqual.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Invalid precipitationTypeEqual: " + precipitationTypeEqual);
            }
            where.and(weather.precipitationType.eq(pt));
        }

        if (cursor != null && idAfter != null) {
            switch (sb) {
                case CREATED_AT -> {
                    final Instant key;
                    try {
                        key = Instant.parse(cursor);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(
                            "Invalid cursor for createdAt: " + cursor);
                    }
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
                    try {
                        key = Long.parseLong(cursor);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(
                            "Invalid cursor for likeCount: " + cursor);
                    }
                    if (sd.equals(SortDirection.ASCENDING)) {
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
        if (joinWeather) {
            jq.leftJoin(weather).on(weather.id.eq(feed.weatherId));
        }
        List<Feed> rows = jq.where(where).orderBy(primary, tie).limit(limit + 1L).fetch();

        boolean hasNext = rows.size() > limit;
        if (hasNext) {
            rows = new ArrayList<>(rows.subList(0, limit));
        }

        List<FeedResponse> data = rows.stream()
            .map(feedMapper::toResponse)
            .map(this::ensureDefaults)        // ← null 방지
            .toList();

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (hasNext && !rows.isEmpty()) {
            Feed last = rows.get(rows.size() - 1);
            nextCursor = (sb == SortBy.CREATED_AT)
                ? last.getCreatedAt().toString()
                : String.valueOf(last.getLikeCount());
            nextIdAfter = last.getId();

        }
        long totalCount = countAllWithoutCursor(keywordLike, skyStatusEqual, precipitationTypeEqual,
            authorIdEqual, joinWeather);
        return new CursorPageResponseDto<>(data, nextCursor, nextIdAfter, hasNext, totalCount, sb,
            sd);
    }

    private FeedResponse ensureDefaults(FeedResponse r) {
        AuthorDto author = r.author();
        if (author == null) {
            author = new AuthorDto(null, "", null);
        }

        WeatherSummaryDto weatherDto = r.weather();
        if (weatherDto == null) {
            PrecipitationDto precipitation = new PrecipitationDto(PrecipitationType.NONE, 0.0, 0.0);
            TemperatureDto temperature = new TemperatureDto(null, null, null, null);
            weatherDto = new WeatherSummaryDto(null, SkyStatus.CLEAR, precipitation, temperature);
        }

        List<OotdDto> ootds = (r.ootds() != null) ? r.ootds() : List.of();
        long likeCount = r.likeCount();
        long commentCount = r.commentCount();
        boolean likedByMe = r.likedByMe();

        return new FeedResponse(
            r.id(),
            r.createdAt(),
            r.updatedAt(),
            author,
            weatherDto,
            ootds,
            r.content(),
            likeCount,
            commentCount,
            likedByMe
        );
    }

    private long countAllWithoutCursor(
        @Nullable String keywordLike,
        @Nullable String skyStatusEqual,
        @Nullable String precipitationTypeEqual,
        @Nullable UUID authorIdEqual,
        boolean joinWeather
    ) {
        BooleanBuilder where = new BooleanBuilder();
        if (authorIdEqual != null) {
            where.and(feed.authorId.eq(authorIdEqual));
        }
        if (keywordLike != null && !keywordLike.isBlank()) {
            where.and(feed.content.containsIgnoreCase(keywordLike));
        }

        if (skyStatusEqual != null && !skyStatusEqual.isBlank()) {
            final SkyStatus ss;
            try {
                ss = SkyStatus.valueOf(skyStatusEqual.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid skyStatusEqual: " + skyStatusEqual);
            }
            where.and(weather.skyStatus.eq(ss));
        }
        if (precipitationTypeEqual != null && !precipitationTypeEqual.isBlank()) {
            final PrecipitationType pt;
            try {
                pt = PrecipitationType.valueOf(precipitationTypeEqual.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Invalid precipitationTypeEqual: " + precipitationTypeEqual);
            }
            where.and(weather.precipitationType.eq(pt));
        }

        var q = qf.select(feed.count()).from(feed);
        if (joinWeather) {
            q.leftJoin(weather).on(weather.id.eq(feed.weatherId));
        }
        Long cnt = q.where(where).fetchOne();
        return (cnt == null) ? 0L : cnt;
    }
}
