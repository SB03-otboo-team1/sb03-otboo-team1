package com.onepiece.otboo.domain.dm.controller;

import com.onepiece.otboo.domain.dm.dto.request.DirectMessageRequest;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import com.onepiece.otboo.domain.dm.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DirectMessageWsController {

    private final DirectMessageService directMessageService;

    @MessageMapping("/direct-message-send")
    public DirectMessageDto create(@Payload DirectMessageRequest request) {
        log.info("[DirectMessageWsController] DM 전송 요청 - request: {}", request);

        return directMessageService.createDirectMessage(request);
    }
}
