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

    @Test
    @DisplayName("DM 목록 조회 - 커서 페이징 성공")
    void findDirectMessages_success() {
        User sender = User.builder()
            .provider(Provider.LOCAL)
            .email("sender@test.com")
            .password("pw")
            .role(Role.USER)
            .locked(false)
            .build();
        ReflectionTestUtils.setField(sender, "createdAt", Instant.now());
        sender = userRepository.save(sender);

        User receiver = User.builder()
            .provider(Provider.LOCAL)
            .email("receiver@test.com")
            .password("pw")
            .role(Role.USER)
            .locked(false)
            .build();
        ReflectionTestUtils.setField(receiver, "createdAt", Instant.now());
        receiver = userRepository.save(receiver);

        DirectMessage dm = DirectMessage.builder()
            .sender(sender)
            .receiver(receiver)
            .content("첫 번째 DM")
            .build();
        ReflectionTestUtils.setField(dm, "createdAt", Instant.now());

        directMessageRepository.save(dm);

        List<DirectMessageResponse> result =
            directMessageRepository.findDirectMessages(sender.getId(), null, null, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("첫 번째 DM");
    }
}