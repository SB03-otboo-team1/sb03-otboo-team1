package com.onepiece.otboo.domain.dm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DirectMessageRequest {

    @NotNull
    private UUID senderId;

    @NotNull
    private UUID receiverId;

    @NotBlank
    private String content;
}