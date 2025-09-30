package com.onepiece.otboo.domain.dm.mapper;

import com.onepiece.otboo.domain.dm.dto.request.DirectMessageRequest;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageResponse;
import com.onepiece.otboo.domain.dm.entity.DirectMessage;
import com.onepiece.otboo.domain.user.entity.User;

public class DirectMessageMapper {

    public static DirectMessage toEntity(DirectMessageRequest request, User sender, User receiver) {
        return DirectMessage.builder()
            .sender(sender)
            .receiver(receiver)
            .content(request.getContent())
            .build();
    }

    public static DirectMessageResponse toResponse(DirectMessage dm) {
        return DirectMessageResponse.builder()
            .id(dm.getId())
            .createdAt(dm.getCreatedAt())
            .sender(new DirectMessageResponse.UserInfo(dm.getSender().getId(),
                dm.getSender().getEmail()))
            .receiver(new DirectMessageResponse.UserInfo(dm.getReceiver().getId(),
                dm.getReceiver().getEmail()))
            .content(dm.getContent())
            .build();
    }
}