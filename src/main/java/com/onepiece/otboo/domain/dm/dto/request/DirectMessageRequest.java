package com.onepiece.otboo.domain.dm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DirectMessageRequest(
    @NotNull
    UUID senderId,
    @NotNull
    UUID receiverId,
    @NotBlank
    String content
) {

}