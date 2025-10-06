package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.QClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.QClothesAttributeOptions;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ClothesAttributeDefCustomRepositoryImpl implements ClothesAttributeDefCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ClothesAttributeDefs> getClothesAttributeDefs(
        SortBy sortBy, SortDirection sortDirection, String keywordLike
    ) {

        QClothesAttributeDefs def = QClothesAttributeDefs.clothesAttributeDefs;
        QClothesAttributeOptions opt = QClothesAttributeOptions.clothesAttributeOptions;

        BooleanBuilder where = new BooleanBuilder();

        if (keywordLike != null) {
            where.and(
                def.name.containsIgnoreCase(keywordLike)
                .or(opt.optionValue.containsIgnoreCase(keywordLike))
            );
        }

        OrderSpecifier<?> primary = switch (sortBy) {
            case CREATED_AT ->
                (sortDirection != null && sortDirection.equals(SortDirection.DESCENDING)
                    ? def.createdAt.desc() : def.createdAt.asc());
            case NAME -> (sortDirection != null && sortDirection.equals(SortDirection.DESCENDING)
                ? def.name.desc() : def.name.asc());
            default -> throw new IllegalStateException("Unexpected value: " + sortBy);
        };
        OrderSpecifier<?> tieBreaker = (sortDirection.equals(SortDirection.DESCENDING)
            ? def.id.desc() : def.id.asc());

        List<ClothesAttributeDefs> defList = jpaQueryFactory
            .selectDistinct(def)
            .from(def)
            .leftJoin(opt).on(opt.definition.eq(def))
            .where(where)
            .orderBy(primary, tieBreaker)
            .fetch();

        return defList;
    }

    @Override
    public Long countClothesAttributeDefs(String keywordLike) {
        QClothesAttributeDefs def = QClothesAttributeDefs.clothesAttributeDefs;
        QClothesAttributeOptions opt = QClothesAttributeOptions.clothesAttributeOptions;

        // 기본 조건
        BooleanBuilder where = new BooleanBuilder();

        if (keywordLike != null) {
            where.and(
                def.name.containsIgnoreCase(keywordLike)
                .or(opt.optionValue.containsIgnoreCase(keywordLike)));
        }

        Long counts = jpaQueryFactory
            .select(def.countDistinct())
            .from(def)
            .leftJoin(opt).on(opt.definition.eq(def))
            .where(where)
            .fetchOne();

        return counts != null ? counts : 0L;
    }
}
