package com.onepiece.otboo.global.event.event;

import com.onepiece.otboo.domain.clothes.dto.data.ClothesAttributeDefDto;
import java.time.Instant;

public record ClothesAttributeAddedEvent(
    ClothesAttributeDefDto data,
    Instant createdAt
) {

}