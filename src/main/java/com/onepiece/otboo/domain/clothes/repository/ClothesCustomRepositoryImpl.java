package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.entity.QClothes;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ClothesCustomRepositoryImpl implements ClothesCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Clothes> getClothesWithCursor(UUID ownerId, String cursor, UUID idAfter,
        int limit, SortBy sortBy, SortDirection sortDirection, ClothesType typeEqual) {

        QClothes clothes = QClothes.clothes;

        // 기본 조건
        BooleanBuilder where = new BooleanBuilder();

        where.and(clothes.owner.id.eq(ownerId));

        if (typeEqual != null) {
            where.and(clothes.type.eq(typeEqual));
        }

        if (cursor != null && idAfter != null) {
            switch (sortBy) {
                case CREATED_AT -> {
                    Instant cAt = Instant.parse(cursor);
                    if (sortDirection.equals(SortDirection.DESCENDING)) {
                        where.and(clothes.createdAt.lt(cAt)
                            .or(clothes.createdAt.eq(cAt).and(clothes.id.lt(idAfter))));
                    } else {
                        where.and(clothes.createdAt.gt(cAt)
                            .or(clothes.createdAt.eq(cAt).and(clothes.id.gt(idAfter))));
                    }
                }
                case NAME -> {
                    if (sortDirection.equals(SortDirection.ASCENDING)) {
                        where.and(clothes.name.gt(cursor)
                            .or(clothes.name.eq(cursor).and(clothes.id.gt(idAfter))));
                    } else {
                        where.and(clothes.name.lt(cursor)
                            .or(clothes.name.eq(cursor).and(clothes.id.lt(idAfter))));
                    }
                }
            }
        }

        OrderSpecifier<?> primary = switch (sortBy) {
            case CREATED_AT ->
                (sortDirection != null && sortDirection.equals(SortDirection.DESCENDING)
                    ? clothes.createdAt.desc() : clothes.createdAt.asc());
            case NAME -> (sortDirection != null && sortDirection.equals(SortDirection.DESCENDING)
                ? clothes.name.desc() : clothes.name.asc());
            default -> throw new IllegalStateException("Unexpected value: " + sortBy);
        };
        OrderSpecifier<?> tieBreaker = (sortDirection.equals(SortDirection.DESCENDING)
            ? clothes.id.desc() : clothes.id.asc());

        List<Clothes> result = jpaQueryFactory
            .select(clothes)
            .from(clothes)
            .where(where)
            .orderBy(primary, tieBreaker)
            .limit(limit + 1)
            .fetch();

        return result;
    }

    @Override
    public Long countClothes(UUID ownerId, ClothesType typeEqual) {
        QClothes clothes = QClothes.clothes;

        BooleanBuilder where = new BooleanBuilder();
        where.and(clothes.owner.id.eq(ownerId));
        if (typeEqual != null) {
            where.and(clothes.type.eq(typeEqual));
        }
        Long count = jpaQueryFactory
            .select(clothes.count())
            .from(clothes)
            .where(where)
            .fetchOne();

        return count != null ? count : 0L;
    }
}
