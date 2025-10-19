package com.onepiece.otboo.domain.location.repository;

import static com.onepiece.otboo.domain.location.entity.QLocation.location;

import com.onepiece.otboo.domain.location.entity.Location;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LocationRepositoryCustomImpl implements LocationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Location> findNearest(double latitude, double longitude) {
        NumberTemplate<Double> distExpr = Expressions.numberTemplate(Double.class,
            "POWER({0} - {1}, 2) + POWER({2} - {3}, 2)",
            location.longitude, longitude,
            location.latitude,  latitude
        );

        return Optional.ofNullable(
            queryFactory.selectFrom(location)
                .orderBy(distExpr.asc())
                .limit(1)
                .fetchOne()
        );
    }
}
