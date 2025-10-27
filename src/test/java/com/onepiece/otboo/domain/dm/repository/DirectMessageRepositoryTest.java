package com.onepiece.otboo.domain.dm.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onepiece.otboo.domain.dm.entity.DirectMessage;
import com.onepiece.otboo.domain.dm.fixture.DirectMessageFixture;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.config.TestJpaConfig;
import com.onepiece.otboo.global.config.TestJpaConfig.MutableDateTimeProvider;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class DirectMessageRepositoryTest {

    @Autowired
    private DirectMessageRepository directMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MutableDateTimeProvider time;

    private UUID userId1, userId2, id1, id2, id3;

    @BeforeEach
    void setUp() {

        User user1 = UserFixture.createUser("han@test.com");
        User user2 = UserFixture.createUser("shin@test.com");
        userRepository.saveAll(List.of(user1, user2));

        time.setNow(Instant.parse("2025-10-15T08:00:00Z"));
        DirectMessage dm1 = DirectMessageFixture.createDirectMessage(user1, user2, "message1");
        directMessageRepository.save(dm1);

        time.setNow(Instant.parse("2025-10-15T09:00:00Z"));
        DirectMessage dm2 = DirectMessageFixture.createDirectMessage(user2, user1, "message2");
        directMessageRepository.save(dm2);

        time.setNow(Instant.parse("2025-10-15T10:00:00Z"));
        DirectMessage dm3 = DirectMessageFixture.createDirectMessage(user1, user2, "message3");
        directMessageRepository.save(dm3);

        userId1 = user1.getId();
        userId2 = user2.getId();
        id1 = dm1.getId();
        id2 = dm2.getId();
        id3 = dm3.getId();
    }

    @Test
    void limit보다_한개_더_많이_조회한다() {

        // when
        List<DirectMessage> result = directMessageRepository.findDirectMessages(userId1, userId2,
            null, null, 2);

        // then
        assertEquals(3, result.size());
    }

    @Test
    void 두번째_페이지_커서_조회_테스트() {

        // given
        List<DirectMessage> first = directMessageRepository.findDirectMessages(userId1, userId2,
            null, null, 2);
        DirectMessage last = first.get(1);

        String cursor = last.getCreatedAt().toString();
        UUID nextIdAfter = last.getId();

        // when
        List<DirectMessage> result = directMessageRepository.findDirectMessages(userId1, userId2,
            cursor, nextIdAfter, 2);

        // then
        assertEquals(1, result.size());
        assertEquals(id1, result.get(0).getId());
        assertEquals("message1", result.get(0).getContent());
    }


    @Test
    void 조건에_맞는_데이터_개수_조회_테스트() {

        // when
        Long count = directMessageRepository.countConversation(userId1, userId2);

        // then
        assertEquals(3L, count);
    }

    @AfterEach
    void tearDown() {
        directMessageRepository.deleteAll();
        userRepository.deleteAll();
    }
}