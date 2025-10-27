package com.onepiece.otboo.domain.recommendation.repository;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import java.util.List;
import java.util.UUID;

public interface RecommendationCustomRepository {

    List<Clothes> getClothesByOwnerIdAndAttributesAndParameters(UUID ownerId,
        UUID parameterId);
}
