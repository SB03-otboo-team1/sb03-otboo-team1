package com.onepiece.otboo.domain.dm.service;

import com.onepiece.otboo.domain.dm.dto.request.DirectMessageRequest;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageResponse;
import java.util.List;
import java.util.UUID;

public interface DirectMessageService {

    DirectMessageResponse createDirectMessage(DirectMessageRequest request);

    DirectMessageResponse getDirectMessageById(UUID id);

    List<DirectMessageResponse> getDirectMessages(
        UUID userId,
        String cursor,
        UUID idAfter,
        int limit,
        String sort
    );

}