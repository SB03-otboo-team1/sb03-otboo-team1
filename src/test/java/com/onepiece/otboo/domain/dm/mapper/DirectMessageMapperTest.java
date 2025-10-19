package com.onepiece.otboo.domain.dm.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import com.onepiece.otboo.domain.dm.entity.DirectMessage;
import com.onepiece.otboo.domain.dm.fixture.DirectMessageFixture;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import com.onepiece.otboo.global.storage.S3Storage;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

class DirectMessageMapperTest {

    private final DirectMessageMapper mapper = Mappers.getMapper(DirectMessageMapper.class);

    @Test
    @DisplayName("Entity → Response 매핑 성공")
    void toResponse_success() {

        // given
        User sender = UserFixture.createUser("sender@test.com");
        ReflectionTestUtils.setField(sender, "id", UUID.randomUUID());

        User receiver = UserFixture.createUser("receiver@test.com");
        ReflectionTestUtils.setField(receiver, "id", UUID.randomUUID());

        DirectMessage dm = DirectMessageFixture.createDirectMessage(sender, receiver,
            "엔티티 → 응답 매핑");

        S3Storage s3 = mock(S3Storage.class);
        given(s3.generatePresignedUrl(any()))
            .willReturn("sender.png");

        UUID dmId = UUID.randomUUID();
        Instant now = Instant.now();
        ReflectionTestUtils.setField(dm, "id", dmId);
        ReflectionTestUtils.setField(dm, "createdAt", now);

        // when
        DirectMessageDto response = mapper.toDto(dm, s3);

        // then
        assertEquals(sender.getId(), response.sender().userId());
        assertEquals(receiver.getId(), response.receiver().userId());
        assertEquals("엔티티 → 응답 매핑", response.content());
    }
}