package com.onepiece.otboo.domain.dm.service;

import com.onepiece.otboo.domain.dm.dto.request.DirectMessageRequest;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.UUID;

public interface DirectMessageService {

    DirectMessageDto createDirectMessage(DirectMessageRequest request);

    CursorPageResponseDto<DirectMessageDto> getDirectMessages(
        UUID userId,
        String cursor,
        UUID idAfter,
        int limit
    );
}