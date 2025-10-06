package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import java.util.List;

/**
 * ClothesAttributeDefs Repository 커스텀 인터페이스
 * QueryDSL을 사용한 복잡한 쿼리를 정의합니다.
 */
public interface ClothesAttributeDefCustomRepository {
    /**
     * 커서 기반 페이징으로 의상 목록을 조회합니다.
     *
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @param keywordLike 검색 키워드
     * @return 커서 페이징된 의상 목록
     */
    List<ClothesAttributeDefDto> getClothesAttributeDefs(
        SortBy sortBy, SortDirection sortDirection, String keywordLike);

    Long countClothesAttributeDefs(String keywordLike);
}
