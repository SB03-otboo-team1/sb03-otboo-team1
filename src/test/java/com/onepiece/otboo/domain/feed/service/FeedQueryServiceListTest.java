package com.onepiece.otboo.domain.feed.service;

import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.mapper.FeedMapper;
import com.onepiece.otboo.domain.weather.dto.response.PrecipitationDto;
import com.onepiece.otboo.domain.weather.dto.response.TemperatureDto;
import com.onepiece.otboo.domain.weather.dto.response.WeatherSummaryDto;
import com.onepiece.otboo.domain.weather.enums.PrecipitationType;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.onepiece.otboo.domain.feed.entity.QFeed.feed;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FeedQueryService#listFeeds 유닛 테스트 완성본
 * - leftJoin().on() 체인까지 모두 self-return 스텁 → NPE 제거
 * - 커서/정렬/필터 분기 + ensureDefaults + hasNext/totalCount 커버
 * - 테스트 메서드명 한글화
 */
class FeedQueryServiceListTest {

    JPAQueryFactory qf;
    FeedMapper feedMapper;
    FeedQueryService sut;

    @BeforeEach
    void setUp() {
        qf = mock(JPAQueryFactory.class);
        feedMapper = mock(FeedMapper.class);
        sut = new FeedQueryService(qf, feedMapper);
    }

    // -------------------- 예외 케이스 --------------------

    @Test
    @DisplayName("잘못된 sortBy → IllegalArgumentException")
    void 잘못된_sortBy_예외() {
        assertThrows(IllegalArgumentException.class, () ->
            sut.listFeeds(null, null, 10, "CREATED_AT", "DESCENDING",
                null, null, null, null, null));
    }

    @Test
    @DisplayName("잘못된 sortDirection → IllegalArgumentException")
    void 잘못된_sortDirection_예외() {
        assertThrows(IllegalArgumentException.class, () ->
            sut.listFeeds(null, null, 10, "createdAt", "DESC",
                null, null, null, null, null));
    }

    @Test
    @DisplayName("createdAt 정렬인데 커서가 Instant가 아님 → IllegalArgumentException")
    void 커서형식오류_createdAt_예외() {
        assertThrows(IllegalArgumentException.class, () ->
            sut.listFeeds("NOT_INSTANT", UUID.randomUUID(), 10, "createdAt", "DESCENDING",
                null, null, null, null, null));
    }

    @Test
    @DisplayName("likeCount 정렬인데 커서가 숫자가 아님 → IllegalArgumentException")
    void 커서형식오류_likeCount_예외() {
        assertThrows(IllegalArgumentException.class, () ->
            sut.listFeeds("NaN", UUID.randomUUID(), 10, "likeCount", "ASCENDING",
                null, null, null, null, null));
    }

    @Test
    @DisplayName("skyStatusEqual 파싱 실패 → IllegalArgumentException")
    void skyStatus_파싱실패_예외() {
        assertThrows(IllegalArgumentException.class, () ->
            sut.listFeeds(null, null, 10, "createdAt", "DESCENDING",
                null, "SUNNY", null, null, null));
    }

    @Test
    @DisplayName("precipitationTypeEqual 파싱 실패 → IllegalArgumentException")
    void precipitationType_파싱실패_예외() {
        assertThrows(IllegalArgumentException.class, () ->
            sut.listFeeds(null, null, 10, "createdAt", "DESCENDING",
                null, null, "RAINNY", null, null));
    }

    // -------------------- 정상 케이스 --------------------

    @SuppressWarnings({"rawtypes","unchecked"})
    @Test
    @DisplayName("createdAt/DESC, limit=2 → fetch 3개(=limit+1)면 hasNext=true & totalCount=42, next 계산 OK")
    void 정상_createdAt_DESC_hasNext_true() {
        // --- feed 목록용 체인 (self-return 전부 스텁) ---
        JPAQuery feedQuery = mock(JPAQuery.class);
        doReturn(feedQuery).when(qf).selectFrom(feed);
        doReturn(feedQuery).when(feedQuery).where(any(Predicate.class));
        doReturn(feedQuery).when(feedQuery).orderBy(any(OrderSpecifier.class), any(OrderSpecifier.class));
        doReturn(feedQuery).when(feedQuery).limit(anyLong());
        // joinWeather=false여도 안전하게 leftJoin/on 스텁(호출돼도 self-return)
        doReturn(feedQuery).when(feedQuery).leftJoin(any(EntityPath.class));
        doReturn(feedQuery).when(feedQuery).on(any(Predicate.class));

        var row1 = mock(com.onepiece.otboo.domain.feed.entity.Feed.class);
        var row2 = mock(com.onepiece.otboo.domain.feed.entity.Feed.class);
        var row3 = mock(com.onepiece.otboo.domain.feed.entity.Feed.class); // 초과분(커서 판단용)

        // ☆ 트리밍 후 "마지막"으로 쓰일 가능성이 있는 row2에도 필수값 스텁
        doReturn(Instant.parse("2025-01-01T00:00:00Z")).when(row2).getCreatedAt();
        doReturn(99L).when(row2).getLikeCount();
        doReturn(UUID.randomUUID()).when(row2).getId();

        // row3도 방어적으로 스텁
        doReturn(Instant.parse("2025-01-02T00:00:00Z")).when(row3).getCreatedAt();
        doReturn(100L).when(row3).getLikeCount();
        doReturn(UUID.randomUUID()).when(row3).getId();

        // limit=2 → 3개 반환하여 hasNext=true 경로 유도
        doReturn(List.of(row1, row2, row3)).when(feedQuery).fetch();

        // --- countAllWithoutCursor 체인 (leftJoin 포함) ---
        JPAQuery<Long> countQuery = mock(JPAQuery.class);
        doReturn(countQuery).when(qf).select(any(Expression.class));
        doReturn(countQuery).when(countQuery).from(feed);
        doReturn(countQuery).when(countQuery).leftJoin(any(EntityPath.class));
        doReturn(countQuery).when(countQuery).on(any(Predicate.class));
        doReturn(countQuery).when(countQuery).where(any(Predicate.class));
        doReturn(42L).when(countQuery).fetchOne();

        // --- 매핑 (ensureDefaults 경로 검증용) ---
        when(feedMapper.toResponse(any())).thenAnswer(inv -> new FeedResponse(
            UUID.randomUUID(), Instant.now(), Instant.now(),
            null, // author -> ensureDefaults에서 보정
            null, // weather -> ensureDefaults에서 보정
            null, // ootds -> ensureDefaults에서 빈 리스트
            "content", 7L, 3L, false
        ));

        var res = sut.listFeeds(
            null, null, 2, "createdAt", "DESCENDING",
            null, null, null, null, null
        );

        assertNotNull(res);
        assertEquals(2, res.data().size());
        assertTrue(res.hasNext());
        assertEquals(42L, res.totalCount());
        assertEquals("createdAt", res.sortBy());
        assertEquals("DESCENDING", res.sortDirection());

        // ensureDefaults 확인
        var first = res.data().get(0);
        assertNotNull(first.author());
        assertNotNull(first.weather());
        assertNotNull(first.ootds());
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    @Test
    @DisplayName("likeCount/ASC + sky/precip 필터 → joinWeather=true, fetch=limit → hasNext=false & next=null")
    void 정상_likeCount_ASC_joinWeather_hasNext_false() {
        // --- feed 목록용 체인 (joinWeather=true 경로: leftJoin/on 필요한 스텁) ---
        JPAQuery feedQuery = mock(JPAQuery.class);
        doReturn(feedQuery).when(qf).selectFrom(feed);
        doReturn(feedQuery).when(feedQuery).where(any(Predicate.class));
        doReturn(feedQuery).when(feedQuery).orderBy(any(OrderSpecifier.class), any(OrderSpecifier.class));
        doReturn(feedQuery).when(feedQuery).limit(anyLong());
        doReturn(feedQuery).when(feedQuery).leftJoin(any(EntityPath.class));
        doReturn(feedQuery).when(feedQuery).on(any(Predicate.class));

        var row1 = mock(com.onepiece.otboo.domain.feed.entity.Feed.class);
        var row2 = mock(com.onepiece.otboo.domain.feed.entity.Feed.class);
        doReturn(List.of(row1, row2)).when(feedQuery).fetch(); // == limit → hasNext=false

        // --- countAllWithoutCursor 체인 (joinWeather=true 대비 leftJoin/on 스텁) ---
        JPAQuery<Long> countQuery = mock(JPAQuery.class);
        doReturn(countQuery).when(qf).select(any(Expression.class));
        doReturn(countQuery).when(countQuery).from(feed);
        doReturn(countQuery).when(countQuery).leftJoin(any(EntityPath.class));
        doReturn(countQuery).when(countQuery).on(any(Predicate.class));
        doReturn(countQuery).when(countQuery).where(any(Predicate.class));
        doReturn(2L).when(countQuery).fetchOne();

        when(feedMapper.toResponse(any())).thenAnswer(inv -> new FeedResponse(
            UUID.randomUUID(), Instant.now(), Instant.now(),
            new AuthorDto(null, "", null),
            new WeatherSummaryDto(null, SkyStatus.CLEAR,
                new PrecipitationDto(PrecipitationType.NONE, 0.0, 0.0),
                new TemperatureDto(null, null, null, null)),
            List.of(), "", 0L, 0L, false
        ));

        var res = sut.listFeeds(
            "123", null, 2, "likeCount", "ASCENDING",
            "키워드", "CLEAR", "NONE", UUID.randomUUID(), UUID.randomUUID()
        );

        assertNotNull(res);
        assertEquals(2, res.data().size());
        assertFalse(res.hasNext());
        assertNull(res.nextCursor());
        assertNull(res.nextIdAfter());
        assertEquals("likeCount", res.sortBy());
        assertEquals("ASCENDING", res.sortDirection());
    }
}
