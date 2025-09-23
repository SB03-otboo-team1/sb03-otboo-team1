package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.entity.QClothes;
import com.onepiece.otboo.domain.clothes.mapper.ClothesMapper;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClothesCustomRepositoryImpl implements ClothesCustomRepository{

  private final JPAQueryFactory jpaQueryFactory;
  private final ClothesMapper clothesMapper;

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

    if (idAfter != null) {
      where.and(clothes.id.gt(idAfter));
    }

    List<Clothes> result = jpaQueryFactory
        .select(Projections.constructor(Clothes.class,
            clothes.id, clothes.name, clothes.type, clothes.createdAt))
        .from(clothes)
        .where(where)
        .orderBy(clothes.createdAt.asc())
        .limit(limit + 1)
        .fetch();

    boolean hasNext = result.size() > limit;
    if (hasNext) {
      result = result.subList(0, limit);
    }

    List<ClothesDto> resultDto = result.stream()
        .map(clothesMapper::toDto)
        .toList();

    String nextCursor = result.isEmpty() ? null : String.valueOf(result.get(result.size() - 1).getId());
    UUID nextIdAfter = result.isEmpty() ? null : result.get(0).getId();

    // totalCount
    Long totalCount = jpaQueryFactory
        .select(clothes.count())
        .from(clothes)
        .where(clothes.ownerId.eq(ownerId))
        .fetchOne();

    return new CursorPageResponseDto<>(
        resultDto,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        sortBy,
        sortDirection
    );
  }
}
