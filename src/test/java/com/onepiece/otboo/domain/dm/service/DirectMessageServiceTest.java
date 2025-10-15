package com.onepiece.otboo.domain.dm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onepiece.otboo.domain.dm.dto.request.DirectMessageRequest;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import com.onepiece.otboo.domain.dm.entity.DirectMessage;
import com.onepiece.otboo.domain.dm.exception.CannotSendMessageToSelfException;
import com.onepiece.otboo.domain.dm.fixture.DirectMessageDtoFixture;
import com.onepiece.otboo.domain.dm.fixture.DirectMessageFixture;
import com.onepiece.otboo.domain.dm.mapper.DirectMessageMapper;
import com.onepiece.otboo.domain.dm.repository.DirectMessageRepository;
import com.onepiece.otboo.domain.feed.dto.response.AuthorDto;
import com.onepiece.otboo.domain.feed.fixture.AuthorDtoFixture;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.event.event.DirectMessageCreatedEvent;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class DirectMessageServiceTest {

    @Mock
    private DirectMessageRepository directMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DirectMessageMapper directMessageMapper;

    @Mock
    private FileStorage storage;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private DirectMessageServiceImpl directMessageService;

    private UUID senderId;
    private UUID receiverId;
    private User sender;
    private User receiver;
    private AuthorDto senderDto;
    private AuthorDto receiverDto;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();

        sender = UserFixture.createUser("sender@test.com");
        receiver = UserFixture.createUser("receiver@test.com");
        senderDto = AuthorDtoFixture.createAuthorDto(senderId, "sender");
        receiverDto = AuthorDtoFixture.createAuthorDto(receiverId, "receiver");

        ReflectionTestUtils.setField(sender, "id", senderId);
        ReflectionTestUtils.setField(receiver, "id", receiverId);

        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getUserId()).thenReturn(senderId);

        Authentication auth =
            new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("DM 생성 성공")
    void createDirectMessage_success() {
        DirectMessageRequest request = new DirectMessageRequest(senderId, receiverId, "테스트 메시지");

        DirectMessage dm = DirectMessageFixture.createDirectMessage(sender, receiver,
            request.content());

        DirectMessageDto mockResponse = DirectMessageDtoFixture.createDirectMessageDto(senderDto,
            receiverDto,
            request.content());

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(directMessageMapper.toDto(dm, storage)).thenReturn(mockResponse);
        when(directMessageRepository.save(any(DirectMessage.class))).thenReturn(dm);

        DirectMessageDto response = directMessageService.createDirectMessage(request);

        assertThat(response.content()).isEqualTo("테스트 메시지");
        assertThat(response.sender().userId()).isEqualTo(senderId);
        ArgumentCaptor<DirectMessageCreatedEvent> captor = ArgumentCaptor.forClass(
            DirectMessageCreatedEvent.class);
        verify(publisher, times(1)).publishEvent(captor.capture());
    }

    @Test
    @DisplayName("자기 자신에게 DM 전송 시 예외 발생")
    void createDirectMessage_selfMessage_fail() {
        UUID userId = UUID.randomUUID();
        DirectMessageRequest request =
            new DirectMessageRequest(userId, userId, "자기 자신에게 보내기");

        assertThatThrownBy(() -> directMessageService.createDirectMessage(request))
            .isInstanceOf(CannotSendMessageToSelfException.class);
    }

    @Test
    @DisplayName("DM 목록 조회 성공 - 인증정보 기반 myId 사용")
    void getDirectMessages_success_withAuthentication() {

        // given
        DirectMessage dm = DirectMessage.builder()
            .sender(sender).receiver(receiver).content("목록 조회 메시지").build();

        DirectMessageDto mockResponse = DirectMessageDto.builder()
            .id(UUID.randomUUID()).sender(senderDto).receiver(receiverDto)
            .content("목록 조회 메시지").build();

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));   // me
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver)); // other
        when(directMessageRepository.findDirectMessages(
            eq(senderId), eq(receiverId), eq(null), eq(null), eq(10))
        ).thenReturn(List.of(dm));
        when(directMessageRepository.countConversation(senderId, receiverId)).thenReturn(1L);
        when(directMessageMapper.toDto(dm, storage)).thenReturn(mockResponse);

        // when
        CursorPageResponseDto<DirectMessageDto> result =
            directMessageService.getDirectMessages(receiverId, null, null, 10);

        // then
        assertEquals(1, result.data().size());
        assertThat(result.data().get(0).content()).isEqualTo("목록 조회 메시지");
        assertThat(result.hasNext()).isFalse();
        assertThat(result.totalCount()).isEqualTo(1L);
        verify(directMessageRepository).findDirectMessages(senderId, receiverId, null, null, 10);
        verify(directMessageMapper).toDto(dm, storage);
    }

    @Test
    @DisplayName("DM 목록 조회 - 다음 페이지가 있는 경우(hasNext=true) 커서/ID 세팅")
    void getDirectMessages_hasNext_setsCursorAndIdAfter() {
        // given
        // 메시지 3개 생성 (limit=2 로 요청하여 hasNext=true 유도)
        DirectMessage dm1 = DirectMessageFixture.createDirectMessage(sender, receiver, "message1");
        DirectMessage dm2 = DirectMessageFixture.createDirectMessage(sender, receiver, "message2");
        DirectMessage dm3 = DirectMessageFixture.createDirectMessage(sender, receiver, "message3");

        // createdAt & id 주입 (리포지토리에서 정렬된 상태로 내려온다고 가정: 최신순 m1 -> m2 -> m3)
        Instant t1 = Instant.now();
        Instant t2 = t1.minusSeconds(10);
        Instant t3 = t2.minusSeconds(10);

        ReflectionTestUtils.setField(dm1, "createdAt", t1);
        ReflectionTestUtils.setField(dm2, "createdAt", t2);
        ReflectionTestUtils.setField(dm3, "createdAt", t3);

        DirectMessageDto dto1 = DirectMessageDtoFixture.createDirectMessageDto(senderDto,
            receiverDto, "message1");
        DirectMessageDto dto2 = DirectMessageDtoFixture.createDirectMessageDto(senderDto,
            receiverDto, "message2");

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));   // me
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver)); // other
        when(directMessageRepository.findDirectMessages(
            eq(senderId), eq(receiverId), eq(null), eq(null), eq(2))
        ).thenReturn(List.of(dm1, dm2, dm3));
        when(directMessageRepository.countConversation(senderId, receiverId)).thenReturn(3L);
        when(directMessageMapper.toDto(dm1, storage)).thenReturn(dto1);
        when(directMessageMapper.toDto(dm2, storage)).thenReturn(dto2);

        // when
        CursorPageResponseDto<DirectMessageDto> result =
            directMessageService.getDirectMessages(receiverId, null, null, 2);

        // then
        assertThat(result.data()).hasSize(2);
        assertEquals("message1", result.data().get(0).content());
        assertEquals("message2", result.data().get(1).content());
        assertTrue(result.hasNext());
        assertEquals(result.nextCursor(), t2.toString());
        assertThat(result.totalCount()).isEqualTo(3L);
        verify(directMessageRepository, times(1))
            .findDirectMessages(senderId, receiverId, null, null, 2);
        verify(directMessageMapper, times(1)).toDto(dm1, storage);
        verify(directMessageMapper, times(1)).toDto(dm2, storage);
    }
}