package com.onepiece.otboo.domain.dm.fixture;

import com.onepiece.otboo.domain.dm.entity.DirectMessage;
import com.onepiece.otboo.domain.user.entity.User;

public class DirectMessageFixture {

    public static DirectMessage createDirectMessage(User sender, User receiver, String content) {
        return DirectMessage.builder()
            .sender(sender)
            .receiver(receiver)
            .content(content)
            .build();
    }
}
