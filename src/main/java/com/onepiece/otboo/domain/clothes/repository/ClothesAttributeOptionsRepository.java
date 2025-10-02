package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeOptions;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesAttributeOptionsRepository extends
    JpaRepository<ClothesAttributeOptions, UUID> {

    List<ClothesAttributeOptions> findByDefinitionId(UUID definitionId);

    List<ClothesAttributeOptions> findByDefinitionIdIn(Set<UUID> defIds);
}
