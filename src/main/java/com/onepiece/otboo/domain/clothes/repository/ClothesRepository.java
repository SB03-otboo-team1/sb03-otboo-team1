package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 의상 데이터 접근을 위한 Repository 인터페이스
 */
@Repository
public interface ClothesRepository extends JpaRepository<Clothes, UUID>, ClothesCustomRepository {

    List<Clothes> getClothesByType(ClothesType clothesType);

    List<Clothes> getClothesByOwnerIdAndType(UUID id, ClothesType type);

    int countClothesByType(ClothesType type);

    int countClothesByOwnerIdAndType(UUID id, ClothesType type);

    List<Clothes> getClothesByOwnerId(UUID id);
}
