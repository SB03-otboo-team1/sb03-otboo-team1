package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesAttributeDefRepository extends JpaRepository<ClothesAttributeDefs, UUID>,
    ClothesAttributeDefCustomRepository {

    Optional<ClothesAttributeDefs> findByName(String name);
}
