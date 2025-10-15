//package com.onepiece.otboo.domain.dm.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//
//import com.onepiece.otboo.domain.dm.dto.request.DirectMessageRequest;
//import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
//import com.onepiece.otboo.domain.dm.entity.DirectMessage;
//import com.onepiece.otboo.domain.dm.exception.CannotSendMessageToSelfException;
//import com.onepiece.otboo.domain.dm.exception.DirectMessageNotFoundException;
//import com.onepiece.otboo.domain.dm.repository.DirectMessageRepository;
//import com.onepiece.otboo.domain.user.entity.User;
//import com.onepiece.otboo.domain.user.enums.Role;
//import com.onepiece.otboo.domain.user.repository.UserRepository;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.test.util.ReflectionTestUtils;
//
//class DirectMessageServiceTest {
//
//    @Mock
//    private DirectMessageRepository directMessageRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private DirectMessageServiceImpl directMessageService;
//
//    private UUID senderId;
//    private UUID receiverId;
//    private User sender;
//    private User receiver;
//
//    @BeforeEach
//    void setup() {
//        MockitoAnnotations.openMocks(this);
//        senderId = UUID.randomUUID();
//        receiverId = UUID.randomUUID();
//
//        sender = User.builder()
//            .email("sender@test.com")
//            .role(Role.USER)
//            .password("password")
//            .locked(false)
//            .build();
//
//        receiver = User.builder()
//            .email("receiver@test.com")
//            .role(Role.USER)
//            .password("password")
//            .locked(false)
//            .build();
//
//        ReflectionTestUtils.setField(sender, "id", senderId);
//        ReflectionTestUtils.setField(receiver, "id", receiverId);
//    }
//
//    @Test
//    @DisplayName("DM 생성 성공")
//    void createDirectMessage_success() {
//        DirectMessageRequest request = new DirectMessageRequest(senderId, receiverId, "테스트 메시지");
//
//        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
//        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
//        when(directMessageRepository.save(any(DirectMessage.class)))
//            .thenAnswer(invocation -> invocation.getArgument(0));
//
//        DirectMessageDto response = directMessageService.createDirectMessage(request);
//
//        assertThat(response.getContent()).isEqualTo("테스트 메시지");
//        assertThat(response.getSender().getEmail()).isEqualTo("sender@test.com");
//    }
//
//    @Test
//    @DisplayName("자기 자신에게 DM 전송 시 예외 발생")
//    void createDirectMessage_selfMessage_fail() {
//        UUID userId = UUID.randomUUID();
//        DirectMessageRequest request =
//            new DirectMessageRequest(userId, userId, "자기 자신에게 보내기");
//
//        assertThatThrownBy(() -> directMessageService.createDirectMessage(request))
//            .isInstanceOf(CannotSendMessageToSelfException.class);
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 DM 조회 시 예외 발생")
//    void getDirectMessage_notFound_fail() {
//        UUID invalidId = UUID.randomUUID();
//
//        when(directMessageRepository.findById(invalidId))
//            .thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> directMessageService.getDirectMessageById(invalidId))
//            .isInstanceOf(DirectMessageNotFoundException.class);
//    }
//
//    @Test
//    @DisplayName("DM 단건 조회 성공")
//    void getDirectMessageById_success() {
//        DirectMessage dm = DirectMessage.builder()
//            .sender(sender)
//            .receiver(receiver)
//            .content("조회 성공 메시지")
//            .build();
//
//        ReflectionTestUtils.setField(dm, "id", UUID.randomUUID());
//
//        when(directMessageRepository.findById(dm.getId()))
//            .thenReturn(Optional.of(dm));
//
//        DirectMessageDto response = directMessageService.getDirectMessageById(dm.getId());
//
//        assertThat(response.getContent()).isEqualTo("조회 성공 메시지");
//        assertThat(response.getSender().getEmail()).isEqualTo("sender@test.com");
//    }
//
//    @Test
//    @DisplayName("DM 목록 조회 성공")
//    void getDirectMessages_success() {
//        DirectMessageDto mockResponse = DirectMessageDto.builder()
//            .id(UUID.randomUUID())
//            .sender(new DirectMessageDto.UserDto(senderId, "sender@test.com"))
//            .receiver(new DirectMessageDto.UserDto(receiverId, "receiver@test.com"))
//            .content("목록 조회 메시지")
//            .build();
//
//        when(directMessageRepository.findDirectMessages(eq(senderId), eq(null), eq(null), eq(10),
//            eq(null)))
//            .thenReturn(List.of(mockResponse));
//
//        var result = directMessageService.getDirectMessages(senderId, null, null, 10, null);
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getContent()).isEqualTo("목록 조회 메시지");
//    }
//
//}