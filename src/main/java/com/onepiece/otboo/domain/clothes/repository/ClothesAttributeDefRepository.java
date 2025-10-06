package com.onepiece.otboo.domain.clothes.repository;

import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.enums.SortBy;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesAttributeDefRepository extends JpaRepository<ClothesAttributeDefs, UUID> {

    List<ClothesAttributeDefs> getClothesAttributeDefsWithCursor(
        SortBy sortBy, SortDirection sortDirection, String keywordLike);

    Long countClothes(String keywordLike);
}
