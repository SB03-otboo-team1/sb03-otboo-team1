package com.onepiece.otboo.domain.dm.repository;

import com.onepiece.otboo.domain.dm.entity.DirectMessage;
import java.util.List;
import java.util.UUID;

public interface DirectMessageRepositoryCustom {

    List<DirectMessage> findDirectMessages(UUID myId, UUID otherId, String cursor, UUID idAfter,
        int limit);

    long countConversation(UUID myId, UUID otherId);
}