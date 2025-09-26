package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.entity.QClothes;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClothesCustomRepositoryImpl implements ClothesCustomRepository{

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public CursorPageResponseDto<ClothesDto> findCursorPage(UUID ownerId, String cursor, UUID idAfter,
      int limit, String sortBy, String sortDirection, ClothesType typeEqual) {

    QClothes clothes = QClothes.clothes;

    // 기본 조건
    BooleanBuilder where = new BooleanBuilder();

    where.and(clothes.ownerId.eq(ownerId));

    if (typeEqual != null) {
      where.and(clothes.type.eq(typeEqual));
    }

      // 커서 처리 (createdAt + id 복합키)
      Instant cursorCreatedAt = null;
      UUID cursorId = null;
      if (cursor != null) {
          String[] parts = cursor.split("\\|");
          cursorCreatedAt = Instant.parse(parts[0]);
          cursorId = UUID.fromString(parts[1]);

          // createdAt > ? OR (createdAt = ? AND id > ?)
          where.and(
              clothes.createdAt.gt(cursorCreatedAt)
                  .or(clothes.createdAt.eq(cursorCreatedAt)
                      .and(clothes.id.gt(cursorId)))
          );
      }

      // 정렬 방향
      Order order = (sortDirection != null && sortDirection.equalsIgnoreCase("desc"))
          ? Order.DESC
          : Order.ASC;

      // 정렬 기준
      List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
      if ("name".equalsIgnoreCase(sortBy)) {
          orderSpecifiers.add(new OrderSpecifier<>(order, clothes.name));
      } else {
          // 기본 정렬: createdAt + id (안정성 확보)
          orderSpecifiers.add(new OrderSpecifier<>(order, clothes.createdAt));
          orderSpecifiers.add(new OrderSpecifier<>(order, clothes.id));
      }

    List<ClothesDto> result = jpaQueryFactory
        .select(Projections.constructor(ClothesDto.class,
            clothes.id, clothes.ownerId, clothes.name, clothes.imageUrl, clothes.type))
        .from(clothes)
        .where(where)
        .orderBy(clothes.createdAt.asc())
        .limit(limit + 1)
        .fetch();

    boolean hasNext = result.size() > limit;
    if (hasNext) {
      result = result.subList(0, limit);
    }

    String nextCursor = result.isEmpty() ? null : String.valueOf(result.get(result.size() - 1).id());
    UUID nextIdAfter = result.isEmpty() ? null : result.get(result.size()-1).id();

    // totalCount
    Long totalCount = Optional.ofNullable(jpaQueryFactory
        .select(clothes.count())
        .from(clothes)
        .where(where)
        .fetchOne()
    ).orElse(0L);

    return new CursorPageResponseDto<>(
        result,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        sortBy,
        sortDirection
    );
  }
}
