package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.entity.Clothes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * 의상 데이터 접근을 위한 Repository 인터페이스
 */
@Repository
public interface ClothesRepository extends JpaRepository<Clothes, UUID>, ClothesCustomRepository {

}
