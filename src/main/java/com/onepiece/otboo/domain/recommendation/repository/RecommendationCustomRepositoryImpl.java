package com.onepiece.otboo.domain.recommendation.repository;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.QClothes;
import com.onepiece.otboo.domain.clothes.entity.QClothesAttributes;
import com.onepiece.otboo.domain.recommendation.entity.QRecommendationParameter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class RecommendationCustomRepositoryImpl implements RecommendationCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Clothes> getClothesByOwnerIdAndAttributesAndParameters(UUID ownerId,
        UUID parameterId) {
        QClothes clothes = QClothes.clothes;
        QClothesAttributes attributes = QClothesAttributes.clothesAttributes;
        QRecommendationParameter parameter = QRecommendationParameter.recommendationParameter;

        // 기본 조건
        BooleanBuilder where = new BooleanBuilder();
        where.and(clothes.owner.id.eq(ownerId));

        // 계절 필터링
        where = applySeasonFilter(where, attributes);

        List<Clothes> result = jpaQueryFactory
            .select(clothes).distinct()
            .from(attributes)
            .join(attributes.clothes, clothes)
            .where(where)
            .fetch();

        return result;
    }

    private BooleanBuilder applySeasonFilter(BooleanBuilder where,
        QClothesAttributes attributes) {

        Integer month = LocalDate.now().getMonthValue();

        return switch (month) {
            case 1, 2, 3 -> where.and(attributes.optionValue.eq("겨울"));
            case 4 -> where.and(attributes.optionValue.in("봄", "가을"));
            case 5 -> where.and(attributes.optionValue.in("봄", "여름"));
            case 6, 7, 8, 9 -> where.and(attributes.optionValue.in("봄", "여름"));
            case 10 -> where.and(attributes.optionValue.in("봄", "여름", "가을"));
            case 11 -> where.and(attributes.optionValue.in("봄", "가을", "겨울"));
            case 12 -> where.and(attributes.optionValue.in("가을", "겨울"));
            default -> where;
        };
    }
}
