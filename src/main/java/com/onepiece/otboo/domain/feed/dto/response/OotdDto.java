package com.onepiece.otboo.domain.feed.dto.response;

import java.util.List;
import java.util.UUID;

public record OotdDto(
    UUID clothesId,
    String name,
    String imageUrl,
    String type,
    List<OotdAttribute> attributes
) {
    public record OotdAttribute(
        UUID definitionId,
        String definitionName,
        List<String> selectableValues,
        String value
    ) {}
}
