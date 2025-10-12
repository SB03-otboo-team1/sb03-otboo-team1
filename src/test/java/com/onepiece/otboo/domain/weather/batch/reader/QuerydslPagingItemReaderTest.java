package com.onepiece.otboo.domain.weather.batch.reader;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.fixture.LocationFixture;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ExecutionContext;

@ExtendWith(MockitoExtension.class)
class QuerydslPagingItemReaderTest {

    @Mock
    private EntityManagerFactory emf;

    @Mock
    private EntityManager em;

    @Mock
    private JPAQuery<Location> q1;

    @Mock
    private JPAQuery<Location> q2;

    @Mock
    private JPAQuery<Location> q3;

    @Mock
    private JPAQuery<Location> q4; // empty

    private final AtomicInteger callSeq = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        given(emf.createEntityManager()).willReturn(em);
        given(em.isOpen()).willReturn(true);
    }

    @Test
    void Location_데이터_읽기_성공_테스트() throws Exception {
        // given

        // offset/limit 체인: 자기 자신 반환
        for (JPAQuery<Location> q : List.of(q1, q2, q3, q4)) {
            given(q.offset(anyLong())).willReturn(q);
            given(q.limit(anyLong())).willReturn(q);
        }

        int pageSize = 10;
        List<Location> all = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            all.add(LocationFixture.createLocation());
        }

        // fetch 순차 반환: 10 / 10 / 5 / 0
        given(q1.fetch()).willReturn(all.subList(0, 10));
        given(q2.fetch()).willReturn(all.subList(10, 20));
        given(q3.fetch()).willReturn(all.subList(20, 30));
        given(q4.fetch()).willReturn(Collections.emptyList());

        Function<JPAQueryFactory, JPAQuery<Location>> provider = qf -> switch (callSeq.incrementAndGet()) {
            case 1 -> q1;
            case 2 -> q2;
            case 3 -> q3;
            default -> q4;
        };

        QuerydslPagingItemReader<Location> reader =
            new QuerydslPagingItemReader<>(emf, pageSize, provider);

        reader.afterPropertiesSet();

        // when
        List<Location> got = new ArrayList<>();
        Location item;
        while ((item = reader.read()) != null) {
            got.add(item);
        }

        reader.close();

        // then
        assertEquals(30, got.size());
        // 페이지별 offset/limit 검증
        verify(q1).offset(0L);
        verify(q1).limit(10);
        verify(q2).offset(10L);
        verify(q2).limit(10);
        verify(q3).offset(20L);
        verify(q3).limit(10);

        // EM 수명주기
        verify(emf, times(4)).createEntityManager();
        verify(em, times(4)).close();
    }

    @Test
    void fetch_실패_시_예외_테스트() throws Exception {
        // given
        given(q1.offset(anyLong())).willReturn(q1);
        given(q1.limit(anyLong())).willReturn(q1);
        given(q1.fetch()).willThrow(new IllegalStateException("데이터 읽기 실패"));

        QuerydslPagingItemReader<Location> reader =
            new QuerydslPagingItemReader<>(emf, 5, qf -> q1);

        reader.afterPropertiesSet();
        ;
        reader.open(new ExecutionContext());

        // when
        Throwable thrown = catchThrowable(reader::read);
        reader.close();

        // then
        assertThat(thrown)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("데이터 읽기 실패");
        verify(em).close();
    }
}