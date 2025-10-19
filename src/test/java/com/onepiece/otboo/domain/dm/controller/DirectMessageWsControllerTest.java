package com.onepiece.otboo.domain.dm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.domain.dm.dto.request.DirectMessageRequest;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import com.onepiece.otboo.domain.dm.fixture.DirectMessageDtoFixture;
import com.onepiece.otboo.domain.dm.service.DirectMessageService;
import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.feed.fixture.AuthorDtoFixture;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DirectMessageWsControllerTest {

    @Mock
    private DirectMessageService directMessageService;

    @InjectMocks
    private DirectMessageWsController directMessageWsController;

    DirectMessageWsControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void DM_생성_테스트() {

        // given
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        AuthorDto senderDto = AuthorDtoFixture.createAuthorDto(senderId, "sender");
        AuthorDto receiverDto = AuthorDtoFixture.createAuthorDto(receiverId, "receiver");
        DirectMessageRequest request = new DirectMessageRequest(senderId, receiverId, "안녕");
        DirectMessageDto expectedResponse = DirectMessageDtoFixture.createDirectMessageDto(
            senderDto, receiverDto, "안녕");

        given(directMessageService.createDirectMessage(request)).willReturn(expectedResponse);

        // when
        DirectMessageDto result = directMessageWsController.create(request);

        // then
        assertEquals(expectedResponse, result);
        assertEquals("안녕", result.content());
        verify(directMessageService, times(1)).createDirectMessage(request);
    }
}