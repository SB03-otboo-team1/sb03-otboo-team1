package com.onepiece.otboo.domain.dm.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.onepiece.otboo.domain.dm.dto.response.DirectMessageResponse;
import com.onepiece.otboo.domain.dm.entity.DirectMessage;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Provider;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.config.QuerydslConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@Import(QuerydslConfig.class)
class DirectMessageRepositoryTest {

    @Autowired
    private DirectMessageRepository directMessageRepository;

    @Autowired
    private UserRepository userRepository;

    private User createUser(String email) {
        User user = User.builder()
            .provider(Provider.LOCAL)
            .email(email)
            .password("pw")
            .role(Role.USER)
            .locked(false)
            .build();
        ReflectionTestUtils.setField(user, "createdAt", Instant.now());
        return userRepository.save(user);
    }

    @Test
    @DisplayName("DM 목록 조회 - 기본 조회 성공")
    void findDirectMessages_success() {
        User sender = createUser("sender@test.com");
        User receiver = createUser("receiver@test.com");

        DirectMessage dm = DirectMessage.builder()
            .sender(sender)
            .receiver(receiver)
            .content("첫 번째 DM")
            .build();
        ReflectionTestUtils.setField(dm, "createdAt", Instant.now());
        directMessageRepository.save(dm);

        List<DirectMessageResponse> result =
            directMessageRepository.findDirectMessages(sender.getId(), null, null, 10, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("첫 번째 DM");
    }

    @Test
    @DisplayName("DM 목록 조회 - Limit 적용 성공")
    void findDirectMessages_withLimit_success() {
        User sender = createUser("sender@test.com");
        User receiver = createUser("receiver@test.com");

        for (int i = 1; i <= 5; i++) {
            DirectMessage dm = DirectMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content("메시지 " + i)
                .build();
            ReflectionTestUtils.setField(dm, "createdAt", Instant.now().plusSeconds(i));
            directMessageRepository.save(dm);
        }

        List<DirectMessageResponse> result =
            directMessageRepository.findDirectMessages(sender.getId(), null, null, 3, null);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getContent()).isEqualTo("메시지 5");
    }

    @Test
    @DisplayName("DM 목록 조회 - idAfter 기반 커서 페이징 성공")
    void findDirectMessages_withIdAfter_success() {
        User sender = createUser("sender@test.com");
        User receiver = createUser("receiver@test.com");

        Instant baseTime = Instant.now();

        DirectMessage dm1 = DirectMessage.builder()
            .sender(sender)
            .receiver(receiver)
            .content("메시지 1")
            .build();
        ReflectionTestUtils.setField(dm1, "createdAt", baseTime);
        directMessageRepository.save(dm1);

        DirectMessage dm2 = DirectMessage.builder()
            .sender(sender)
            .receiver(receiver)
            .content("메시지 2")
            .build();
        ReflectionTestUtils.setField(dm2, "createdAt", baseTime.plusSeconds(1));
        directMessageRepository.save(dm2);

        DirectMessage dm3 = DirectMessage.builder()
            .sender(sender)
            .receiver(receiver)
            .content("메시지 3")
            .build();
        ReflectionTestUtils.setField(dm3, "createdAt", baseTime.plusSeconds(2));
        directMessageRepository.save(dm3);

        List<DirectMessageResponse> result =
            directMessageRepository.findDirectMessages(sender.getId(), null, dm1.getId(), 10, null);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(DirectMessageResponse::getContent)
            .containsExactly("메시지 3", "메시지 2");
    }

    @Test
    @DisplayName("DM 목록 조회 - DESC 정렬 성공")
    void findDirectMessages_descOrder_success() {
        User sender = createUser("sender@test.com");
        User receiver = createUser("receiver@test.com");

        for (int i = 1; i <= 3; i++) {
            DirectMessage dm = DirectMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content("메시지 " + i)
                .build();
            ReflectionTestUtils.setField(dm, "createdAt", Instant.now().plusSeconds(i));
            directMessageRepository.save(dm);
        }

        List<DirectMessageResponse> result =
            directMessageRepository.findDirectMessages(sender.getId(), null, null, 10,
                "createdAt,DESC");

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getContent()).isEqualTo("메시지 3");
        assertThat(result.get(2).getContent()).isEqualTo("메시지 1");
    }

    @Test
    @DisplayName("DM 목록 조회 - ASC 정렬 성공")
    void findDirectMessages_ascOrder_success() {
        User sender = createUser("sender@test.com");
        User receiver = createUser("receiver@test.com");

        for (int i = 1; i <= 3; i++) {
            DirectMessage dm = DirectMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content("메시지 " + i)
                .build();
            ReflectionTestUtils.setField(dm, "createdAt", Instant.now().plusSeconds(i));
            directMessageRepository.save(dm);
        }

        List<DirectMessageResponse> result =
            directMessageRepository.findDirectMessages(sender.getId(), null, null, 10,
                "createdAt,ASC");

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getContent()).isEqualTo("메시지 1");
        assertThat(result.get(2).getContent()).isEqualTo("메시지 3");
    }
}