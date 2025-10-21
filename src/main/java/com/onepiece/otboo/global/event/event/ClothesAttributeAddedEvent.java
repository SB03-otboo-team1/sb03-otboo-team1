package com.onepiece.otboo.global.event.event;

import com.onepiece.otboo.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import java.time.Instant;
import java.util.UUID;

public record ClothesAttributeAddedEvent(
    UUID userId,
    ClothesAttributeDefCreateRequest data,
    Instant createdAt
) {

}