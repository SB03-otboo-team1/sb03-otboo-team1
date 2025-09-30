package com.onepiece.otboo.domain.dm.service;

import com.onepiece.otboo.domain.dm.dto.request.DirectMessageRequest;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageResponse;
import com.onepiece.otboo.domain.dm.entity.DirectMessage;
import com.onepiece.otboo.domain.dm.exception.CannotSendMessageToSelfException;
import com.onepiece.otboo.domain.dm.exception.DirectMessageNotFoundException;
import com.onepiece.otboo.domain.dm.repository.DirectMessageRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DirectMessageServiceImpl implements DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;

    /**
     * DM 생성
     */
    @Override
    public DirectMessageResponse createDirectMessage(DirectMessageRequest request) {
        if (request.getSenderId().equals(request.getReceiverId())) {
            throw new CannotSendMessageToSelfException();
        }

        User sender = userRepository.findById(request.getSenderId())
            .orElseThrow(() -> new EntityNotFoundException("Sender not found"));

        User receiver = userRepository.findById(request.getReceiverId())
            .orElseThrow(() -> new EntityNotFoundException("Receiver not found"));

        DirectMessage dm = DirectMessage.builder()
            .sender(sender)
            .receiver(receiver)
            .content(request.getContent())
            .build();

        DirectMessage saved = directMessageRepository.save(dm);

        return DirectMessageResponse.builder()
            .id(saved.getId())
            .createdAt(saved.getCreatedAt())
            .sender(new DirectMessageResponse.UserInfo(sender.getId(), sender.getEmail()))
            .receiver(new DirectMessageResponse.UserInfo(receiver.getId(), receiver.getEmail()))
            .content(saved.getContent())
            .build();
    }

    /**
     * DM 목록 조회 (커서 기반 페이징 + 정렬 지원)
     */
    @Override
    @Transactional(readOnly = true)
    public List<DirectMessageResponse> getDirectMessages(
        UUID userId,
        String cursor,
        UUID idAfter,
        int limit,
        String sort
    ) {
        return directMessageRepository.findDirectMessages(userId, cursor, idAfter, limit, sort);
    }

    /**
     * DM 단건 조회
     */
    @Override
    @Transactional(readOnly = true)
    public DirectMessageResponse getDirectMessageById(UUID id) {
        DirectMessage dm = directMessageRepository.findById(id)
            .orElseThrow(DirectMessageNotFoundException::new);

        return DirectMessageResponse.builder()
            .id(dm.getId())
            .createdAt(dm.getCreatedAt())
            .sender(new DirectMessageResponse.UserInfo(
                dm.getSender().getId(), dm.getSender().getEmail()))
            .receiver(new DirectMessageResponse.UserInfo(
                dm.getReceiver().getId(), dm.getReceiver().getEmail()))
            .content(dm.getContent())
            .build();
    }
}