package com.onepiece.otboo.domain.dm.repository;

import com.onepiece.otboo.domain.dm.dto.response.DirectMessageResponse;
import java.util.List;
import java.util.UUID;

public interface DirectMessageRepositoryCustom {

    List<DirectMessageResponse> findDirectMessages(UUID userId, String cursor, UUID idAfter,
        int limit);
}