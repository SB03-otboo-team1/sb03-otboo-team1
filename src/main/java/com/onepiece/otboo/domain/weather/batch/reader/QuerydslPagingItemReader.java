package com.onepiece.otboo.domain.weather.batch.reader;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.lang.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FlushModeType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.AbstractPagingItemReader;

@Slf4j
public class QuerydslPagingItemReader<T> extends AbstractPagingItemReader<T> {

    private final EntityManagerFactory emf;
    private final Function<JPAQueryFactory, JPAQuery<T>> queryProvider;

    @Setter
    private boolean transacted = false;

    public QuerydslPagingItemReader(
        EntityManagerFactory emf,
        int pageSize,
        Function<JPAQueryFactory, JPAQuery<T>> queryProvider
    ) {
        this.emf = emf;
        this.queryProvider = queryProvider;
        setPageSize(pageSize);
        setName(this.getClass().getName());
    }

    @Override
    protected void doReadPage() {
        if (results == null) {
            results = new ArrayList<>(getPageSize());
        } else {
            results.clear();
        }

        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.setFlushMode(FlushModeType.COMMIT);
            em.setProperty("org.hibernate.readOnly", true);

            JPAQueryFactory qf = new JPAQueryFactory(em);

            // 기준 쿼리에 offset/limit만 주입
            JPAQuery<T> base = queryProvider.apply(qf);
            List<T> page = base
                .offset((long) getPage() * getPageSize())
                .limit(getPageSize())
                .fetch();

            results.addAll(page);

            // 트랜잭션 컨텍스트를 최소화
            if (!transacted) {
                em.clear();
            }
        } catch (Exception e) {
            log.error("QuerydslPagingItemReader read page error (page={}, size={}): {}",
                getPage(), getPageSize(), e.getMessage(), e);
            throw e;
        } finally {
            closeQuietly(em);
        }
    }

    private void closeQuietly(@Nullable EntityManager em) {
        if (em != null && em.isOpen()) {
            try {
                em.close();
            } catch (Exception ignore) {
            }
        }
    }
}
