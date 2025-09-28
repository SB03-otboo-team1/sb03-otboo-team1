package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.entity.QClothes;
import com.onepiece.otboo.domain.clothes.exception.InvalidClothesSortException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ClothesCustomRepositoryImpl implements ClothesCustomRepository{

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<Clothes> getClothesWithCursor(UUID ownerId, String cursor, UUID idAfter,
      int limit, String sortBy, String sortDirection, ClothesType typeEqual) {

      QClothes clothes = QClothes.clothes;

      // 기본 조건
      BooleanBuilder where = new BooleanBuilder();

      where.and(clothes.ownerId.eq(ownerId));

      if (typeEqual != null) {
          where.and(clothes.type.eq(typeEqual));
      }

      // 정렬 방향
      Order order = (sortDirection != null && sortDirection.equalsIgnoreCase("desc"))
          ? Order.DESC
          : Order.ASC;

      // 정렬 기준
      List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
      if ("name".equalsIgnoreCase(sortBy)) {
          orderSpecifiers.add(new OrderSpecifier<>(order, clothes.name));
      } else if ("createdAt".equalsIgnoreCase(sortBy)){
          // 기본 정렬: createdAt + id (안정성 확보)
          orderSpecifiers.add(new OrderSpecifier<>(order, clothes.createdAt));
          orderSpecifiers.add(new OrderSpecifier<>(order, clothes.id));
      } else {
          throw new InvalidClothesSortException();
      }

      List<Clothes> result = jpaQueryFactory
          .select(clothes)
          .from(clothes)
          .where(where)
          .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
          .limit(limit + 1)
          .fetch();

      return result;
    }

    @Override
    public Long countClothes(UUID ownerId, ClothesType typeEqual) {
        QClothes clothes = QClothes.clothes;

        BooleanBuilder where = new BooleanBuilder();
        where.and(clothes.ownerId.eq(ownerId));
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
