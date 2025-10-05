package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.entity.ClothesAttributes;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesAttributeRepository extends JpaRepository<ClothesAttributes, UUID> {

    List<ClothesAttributes> findByClothesId(UUID clothesId);

    List<ClothesAttributes> findByClothesIdIn(List<UUID> clothesIds);

    void deleteByClothesId(UUID clothesId);
}
