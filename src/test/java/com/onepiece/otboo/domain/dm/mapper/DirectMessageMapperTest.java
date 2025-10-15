//package com.onepiece.otboo.domain.dm.mapper;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.onepiece.otboo.domain.dm.dto.request.DirectMessageRequest;
//import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
//import com.onepiece.otboo.domain.dm.entity.DirectMessage;
//import com.onepiece.otboo.domain.user.entity.User;
//import com.onepiece.otboo.domain.user.enums.Role;
//import java.time.Instant;
//import java.util.UUID;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.test.util.ReflectionTestUtils;
//
//class DirectMessageMapperTest {
//
//    @Test
//    @DisplayName("Request → Entity 매핑 성공")
//    void toEntity_success() {
//        User sender = User.builder()
//            .email("sender@test.com")
//            .password("pw")
//            .role(Role.USER)
//            .locked(false)
//            .build();
//        ReflectionTestUtils.setField(sender, "id", UUID.randomUUID());
//
//        User receiver = User.builder()
//            .email("receiver@test.com")
//            .password("pw")
//            .role(Role.USER)
//            .locked(false)
//            .build();
//        ReflectionTestUtils.setField(receiver, "id", UUID.randomUUID());
//
//        DirectMessageRequest request =
//            new DirectMessageRequest(sender.getId(), receiver.getId(), "테스트 메시지");
//
//        DirectMessage dm = DirectMessageMapper.toEntity(request, sender, receiver);
//
//        assertThat(dm.getSender()).isEqualTo(sender);
//        assertThat(dm.getReceiver()).isEqualTo(receiver);
//        assertThat(dm.getContent()).isEqualTo("테스트 메시지");
//    }
//
//    @Test
//    @DisplayName("Entity → Response 매핑 성공")
//    void toResponse_success() {
//        User sender = User.builder()
//            .email("sender@test.com")
//            .password("pw")
//            .role(Role.USER)
//            .locked(false)
//            .build();
//        ReflectionTestUtils.setField(sender, "id", UUID.randomUUID());
//
//        User receiver = User.builder()
//            .email("receiver@test.com")
//            .password("pw")
//            .role(Role.USER)
//            .locked(false)
//            .build();
//        ReflectionTestUtils.setField(receiver, "id", UUID.randomUUID());
//
//        DirectMessage dm = DirectMessage.builder()
//            .sender(sender)
//            .receiver(receiver)
//            .content("엔티티 → 응답 매핑")
//            .build();
//
//        UUID dmId = UUID.randomUUID();
//        Instant now = Instant.now();
//        ReflectionTestUtils.setField(dm, "id", dmId);
//        ReflectionTestUtils.setField(dm, "createdAt", now);
//
//        DirectMessageDto response = DirectMessageMapper.toResponse(dm);
//
//        assertThat(response.getId()).isEqualTo(dmId);
//        assertThat(response.getCreatedAt()).isEqualTo(now);
//        assertThat(response.getSender().getId()).isEqualTo(sender.getId());
//        assertThat(response.getReceiver().getId()).isEqualTo(receiver.getId());
//        assertThat(response.getContent()).isEqualTo("엔티티 → 응답 매핑");
//    }
//}