package com.onepiece.otboo.domain.dm.service;

import com.onepiece.otboo.domain.dm.dto.request.DirectMessageRequest;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import java.util.List;
import java.util.UUID;

public interface DirectMessageService {

    DirectMessageDto createDirectMessage(DirectMessageRequest request);

    DirectMessageDto getDirectMessageById(UUID id);

    List<DirectMessageDto> getDirectMessages(
        UUID userId,
        String cursor,
        UUID idAfter,
        int limit,
        String sort
    );

}