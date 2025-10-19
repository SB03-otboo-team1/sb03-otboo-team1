package com.onepiece.otboo.global.event.event;

import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import java.time.Instant;

public record DirectMessageCreatedEvent(
    DirectMessageDto data,
    Instant createdAt
) {

}
