package com.onepiece.otboo.domain.feed.repository;

import com.onepiece.otboo.domain.feed.dto.request.FeedListRequest;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.weather.enums.PrecipitationType; // ← 프로젝트 실제 경로로 맞춰주세요
import com.onepiece.otboo.domain.weather.enums.SkyStatus;        // ← 프로젝트 실제 경로로 맞춰주세요
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.onepiece.otboo.domain.feed.entity.QFeed.feed;
import static com.onepiece.otboo.domain.weather.entity.QWeather.weather;

@Repository
@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepositoryCustom {

    private final JPAQueryFactory qf;

    /**
     * 목록 조회 (limit+1은 호출부에서 자르기 위해 그대로 반환)
     */
    @Override
    public List<Feed> findSlice(FeedListRequest req) {
        // ---- 기본 검증: IllegalArgumentException -> 전역 핸들러에서 400 매핑
        if (req.limit() <= 0 || req.limit() > 100) {
            throw new IllegalArgumentException("limit must be between 1 and 100");
        }

        // ---- sortBy / sortDirection 파싱 (FeedListRequest가 enum이든 String이든 toString()으로 통일)
        final String sortByStr = String.valueOf(req.sortBy());
        final String sortDirStr = String.valueOf(req.sortDirection());

        final boolean sortByCreatedAt = switch (sortByStr) {
            case "createdAt" -> true;
            case "likeCount" -> false;
            default -> throw new IllegalArgumentException("Invalid sortBy: " + sortByStr);
        };
        final boolean ascending = switch (sortDirStr) {
            case "ASCENDING" -> true;
            case "DESCENDING" -> false;
            default -> throw new IllegalArgumentException("Invalid sortDirection: " + sortDirStr);
        };

        // ---- where
        BooleanBuilder where = new BooleanBuilder();

        if (req.authorIdEqual() != null) {
            where.and(feed.authorId.eq(req.authorIdEqual()));
        }
        if (req.keywordLike() != null && !req.keywordLike().isBlank()) {
            where.and(feed.content.containsIgnoreCase(req.keywordLike()));
        }

        boolean joinWeather = false;

        if (req.skyStatusEqual() != null && !req.skyStatusEqual().isBlank()) {
            joinWeather = true;
            final SkyStatus ss;
            try {
                ss = SkyStatus.valueOf(req.skyStatusEqual().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid skyStatusEqual: " + req.skyStatusEqual());
            }
            where.and(weather.skyStatus.eq(ss));
        }

        if (req.precipitationTypeEqual() != null && !req.precipitationTypeEqual().isBlank()) {
            joinWeather = true;
            final PrecipitationType pt;
            try {
                pt = PrecipitationType.valueOf(req.precipitationTypeEqual().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid precipitationTypeEqual: " + req.precipitationTypeEqual());
            }
            where.and(weather.precipitationType.eq(pt));
        }

        // ---- 커서(where): (정렬키, id) keyset
        if (req.cursor() != null && req.idAfter() != null) {
            final String cursor = req.cursor();
            final UUID idAfter = req.idAfter();

            if (sortByCreatedAt) {
                final Instant key;
                try {
                    key = Instant.parse(cursor);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid cursor for createdAt: " + cursor);
                }
                if (ascending) {
                    where.and(feed.createdAt.gt(key)
                        .or(feed.createdAt.eq(key).and(feed.id.gt(idAfter))));
                } else {
                    where.and(feed.createdAt.lt(key)
                        .or(feed.createdAt.eq(key).and(feed.id.lt(idAfter))));
                }
            } else {
                final long key;
                try {
                    key = Long.parseLong(cursor);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid cursor for likeCount: " + cursor);
                }
                if (ascending) {
                    where.and(feed.likeCount.gt(key)
                        .or(feed.likeCount.eq(key).and(feed.id.gt(idAfter))));
                } else {
                    where.and(feed.likeCount.lt(key)
                        .or(feed.likeCount.eq(key).and(feed.id.lt(idAfter))));
                }
            }
        }

        // ---- 정렬
        OrderSpecifier<?> primary = sortByCreatedAt
            ? (ascending ? feed.createdAt.asc() : feed.createdAt.desc())
            : (ascending ? feed.likeCount.asc() : feed.likeCount.desc());
        OrderSpecifier<?> tie = ascending ? feed.id.asc() : feed.id.desc();

        // ---- 조회 (limit+1)
        var q = qf.selectFrom(feed);
        if (joinWeather) {
            q.leftJoin(weather).on(weather.id.eq(feed.weatherId));
        }
        return q.where(where)
            .orderBy(primary, tie)
            .limit(req.limit() + 1L)
            .fetch();
    }

    /**
     * totalCount (커서는 제외, 동일 필터만 적용)
     */
    @Override
    public long countAll(FeedListRequest req) {
        BooleanBuilder where = new BooleanBuilder();

        if (req.authorIdEqual() != null) {
            where.and(feed.authorId.eq(req.authorIdEqual()));
        }
        if (req.keywordLike() != null && !req.keywordLike().isBlank()) {
            where.and(feed.content.containsIgnoreCase(req.keywordLike()));
        }

        boolean joinWeather = false;

        if (req.skyStatusEqual() != null && !req.skyStatusEqual().isBlank()) {
            joinWeather = true;
            final SkyStatus ss;
            try {
                ss = SkyStatus.valueOf(req.skyStatusEqual().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid skyStatusEqual: " + req.skyStatusEqual());
            }
            where.and(weather.skyStatus.eq(ss));
        }

        if (req.precipitationTypeEqual() != null && !req.precipitationTypeEqual().isBlank()) {
            joinWeather = true;
            final PrecipitationType pt;
            try {
                pt = PrecipitationType.valueOf(req.precipitationTypeEqual().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid precipitationTypeEqual: " + req.precipitationTypeEqual());
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
