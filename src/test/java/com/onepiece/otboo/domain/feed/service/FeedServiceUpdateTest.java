package com.onepiece.otboo.domain.feed.service;

import com.onepiece.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.feed.mapper.FeedMapper;
import com.onepiece.otboo.domain.feed.repository.FeedRepository;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceUpdateTest {

    @Mock FeedRepository feedRepository;
    @Mock UserRepository userRepository;
    @Mock WeatherRepository weatherRepository;
    @Mock FeedMapper feedMapper;

    @InjectMocks
    FeedService feedService;

    @Test
    @DisplayName("피드_수정_성공시_내용이_업데이트되고_Response_반환")
    void 피드_수정_성공시_내용이_업데이트되고_Response_반환() {
        UUID feedId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID requesterId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Feed feed = mock(Feed.class);
        when(feed.getAuthorId()).thenReturn(requesterId);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));

        FeedResponse response = mock(FeedResponse.class);
        when(feedMapper.toResponse(feed)).thenReturn(response);

        var req = new FeedUpdateRequest("new content");
        FeedResponse result = feedService.update(feedId, requesterId, req);

        assertNotNull(result);
        assertEquals(response, result);

        InOrder inOrder = inOrder(feedRepository, feed, feedMapper);
        inOrder.verify(feedRepository).findById(feedId);
        inOrder.verify(feed).getAuthorId();
        inOrder.verify(feed).updateContent("new content");
        inOrder.verify(feedMapper).toResponse(feed);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("피드_수정_대상없음_FEED_NOT_FOUND")
    void 피드_수정_대상없음_FEED_NOT_FOUND() {
        UUID feedId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID requesterId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

        GlobalException ex = assertThrows(GlobalException.class,
            () -> feedService.update(feedId, requesterId, new FeedUpdateRequest("x")));
        assertEquals(ErrorCode.FEED_NOT_FOUND, ex.getErrorCode());

        verify(feedRepository, times(1)).findById(feedId);
        verifyNoMoreInteractions(feedRepository);
        verifyNoInteractions(feedMapper);
    }

    @Test
    @DisplayName("피드_수정_작성자불일치_FEED_FORBIDDEN")
    void 피드_수정_작성자불일치_FEED_FORBIDDEN() {
        UUID feedId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        UUID requesterId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        UUID authorId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        Feed feed = mock(Feed.class);
        when(feed.getAuthorId()).thenReturn(authorId);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));

        GlobalException ex = assertThrows(GlobalException.class,
            () -> feedService.update(feedId, requesterId, new FeedUpdateRequest("x")));
        assertEquals(ErrorCode.FEED_FORBIDDEN, ex.getErrorCode());

        verify(feedRepository, times(1)).findById(feedId);
        verify(feed, times(1)).getAuthorId();
        verifyNoMoreInteractions(feedRepository, feed, feedMapper);
    }
}
