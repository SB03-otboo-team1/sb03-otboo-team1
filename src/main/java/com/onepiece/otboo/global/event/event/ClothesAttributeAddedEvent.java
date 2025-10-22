package com.onepiece.otboo.global.event.event;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import java.time.Instant;
import java.util.UUID;

public record ClothesAttributeAddedEvent(
    UUID userId,
    ClothesAttributeDefDto data,
    Instant createdAt
) {

}