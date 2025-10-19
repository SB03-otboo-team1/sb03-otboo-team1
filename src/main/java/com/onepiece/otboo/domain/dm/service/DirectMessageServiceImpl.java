package com.onepiece.otboo.domain.dm.service;

import com.onepiece.otboo.domain.auth.exception.UnAuthorizedException;
import com.onepiece.otboo.domain.dm.dto.request.DirectMessageRequest;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import com.onepiece.otboo.domain.dm.entity.DirectMessage;
import com.onepiece.otboo.domain.dm.exception.CannotSendMessageToSelfException;
import com.onepiece.otboo.domain.dm.mapper.DirectMessageMapper;
import com.onepiece.otboo.domain.dm.repository.DirectMessageRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import com.onepiece.otboo.global.event.event.DirectMessageCreatedEvent;
import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DirectMessageServiceImpl implements DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;
    private final DirectMessageMapper directMessageMapper;
    private final FileStorage storage;
    private final ApplicationEventPublisher publisher;

    /**
     * DM 생성
     */
    @Override
    public DirectMessageDto createDirectMessage(DirectMessageRequest request) {
        if (request.senderId().equals(request.receiverId())) {
            throw new CannotSendMessageToSelfException();
        }

        log.info("[DirectMessageService] DM 생성 요청 - sender: {}, receiver: {}",
            request.senderId(),
            request.receiverId());

        User sender = findUser(request.senderId());

        User receiver = findUser(request.receiverId());

        DirectMessage dm = DirectMessage.builder()
            .sender(sender)
            .receiver(receiver)
            .content(request.content())
            .build();

        DirectMessage saved = directMessageRepository.save(dm);
        DirectMessageDto data = directMessageMapper.toDto(saved, storage);

        publisher.publishEvent(new DirectMessageCreatedEvent(data, Instant.now()));

        return data;
    }

    /**
     * DM 목록 조회 (커서 기반 페이징 + 정렬 지원)
     */
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseDto<DirectMessageDto> getDirectMessages(
        UUID userId,
        String cursor,
        UUID idAfter,
        int limit
    ) {
        UUID myId = getCurrentUserId();
        log.info("[DMService] DM 목록 조회 시작 - myId: {}, userId: {}", myId, userId);

        User me = findUser(myId);
        User other = findUser(userId);

        List<DirectMessage> messages = directMessageRepository.findDirectMessages(
            me.getId(), other.getId(), cursor, idAfter, limit
        );

        boolean hasNext = messages.size() > limit;

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (hasNext) {
            messages = messages.subList(0, limit);
            DirectMessage last = messages.get(messages.size() - 1);
            nextCursor = last.getCreatedAt().toString();
            nextIdAfter = last.getId();
        }

        List<DirectMessageDto> data = messages.stream()
            .map(dm -> directMessageMapper.toDto(dm, storage))
            .toList();

        long totalCount = directMessageRepository.countConversation(me.getId(), other.getId());

        return new CursorPageResponseDto<>(
            data,
            nextCursor,
            nextIdAfter,
            hasNext,
            totalCount,
            SortBy.CREATED_AT,
            SortDirection.DESCENDING
        );
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnAuthorizedException();
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }

        throw new UnAuthorizedException();
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));
    }
}