package com.onepiece.otboo.domain.feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record FeedCreateRequest(
    @NotNull UUID authorId,
    @NotNull UUID weatherId,
    @NotEmpty List<UUID> clothesIds,
    @NotBlank String content
) {
    public List<UUID> distinctClothesIds() {
        return List.copyOf(Set.copyOf(clothesIds));
    }
}