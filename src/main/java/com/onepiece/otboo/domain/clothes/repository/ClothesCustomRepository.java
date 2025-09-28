package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import java.util.List;
import java.util.UUID;

/**
 * Clothes Repository 커스텀 인터페이스
 * QueryDSL을 사용한 복잡한 쿼리를 정의합니다.
 */
public interface ClothesCustomRepository {

  /**
   * 커서 기반 페이징으로 의상 목록을 조회합니다.
   *
   * @param ownerId 소유자 ID
   * @param cursor 커서
   * @param idAfter ID 이후
   * @param limit 페이지 크기
   * @param sortBy 정렬 기준
   * @param sortDirection 정렬 방향
   * @param typeEqual 의상 타입
   * @return 커서 페이징된 의상 목록
   */
  List<Clothes> getClothesWithCursor(UUID ownerId, String cursor, UUID idAfter,
                                               int limit, String sortBy, String sortDirection, 
                                               ClothesType typeEqual);
  Long countClothes(UUID ownerId, ClothesType typeEqual);
}
