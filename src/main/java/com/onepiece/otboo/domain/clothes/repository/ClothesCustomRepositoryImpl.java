package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.entity.QClothes;
import com.onepiece.otboo.domain.clothes.mapper.ClothesMapper;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClothesCustomRepositoryImpl implements ClothesCustomRepository{

  private final JPAQueryFactory queryFactory;

  @Override
  public CursorPageResponse<ClothesDto> findCursorPage(UUID ownerId, String cursor, UUID idAfter,
      int limit, String sortBy, String sortDirection, ClothesType typeEqual) {

    QClothes clothes = QClothes.clothes;

    // 기본 조건
    BooleanBuilder where = new BooleanBuilder();

    where.and(clothes.ownerId.eq(ownerId));

    if (typeEqual != null) {
      where.and(clothes.type.eq(typeEqual));
    }

    if (idAfter != null) {
      where.and(clothes.id.gt(idAfter));
    }

    // 정렬 조건
    Order order = "desc".equalsIgnoreCase(sortDirection) ? Order.DESC : Order.ASC;
    Path<?> sortPath = getSortPath(sortBy, clothes); // 추후 확장 고려

    List<ClothesDto> result = queryFactory
        .select(Projections.constructor(ClothesDto.class,
            clothes.id, clothes.name, clothes.type, clothes.createdAt))
        .from(clothes)
        .where(where)
        .orderBy(new OrderSpecifier<>(order, sortPath))
        .limit(limit + 1)
        .fetch();

    boolean hasNext = result.size() > limit;
    if (hasNext) {
      result = result.subList(0, limit);
    }

    UUID nextCursor = hasNext ? result.get(result.size() - 1).id() : null;

    return new CursorPageResponse<>(result, nextCursor, hasNext);
  }

  private Path<?> getSortPath(String sortBy, QClothes clothes) {
    if ("createdAt".equalsIgnoreCase(sortBy)) {
      return clothes.createdAt;
    }
    if ("name".equalsIgnoreCase(sortBy)) {
      return clothes.name;
    }
    return clothes.id; // 기본 정렬
  }
}
