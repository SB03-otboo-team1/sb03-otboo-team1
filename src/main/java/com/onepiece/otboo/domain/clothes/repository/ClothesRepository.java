package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesDto;
import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * 의상 데이터 접근을 위한 Repository 인터페이스
 */
@Repository
public interface ClothesRepository extends JpaRepository<Clothes, UUID> {

  /**
   * 공개된 의상 목록을 페이징하여 조회합니다.
   */
  List<ClothesDto> findAll();

  /**
   * 특정 타입의 의상 목록을 페이징하여 조회합니다.
   *
   * @param type 의상 타입
   * @return 해당 타입의 의상 목록
   */
  CursorPageResponse<ClothesDto> findByType(ClothesType type);
}
