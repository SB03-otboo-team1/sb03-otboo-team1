package com.onepiece.otboo.domain.feed.service;

import com.onepiece.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.feed.entity.FeedClothes;
import com.onepiece.otboo.domain.feed.mapper.FeedMapper;
import com.onepiece.otboo.domain.feed.repository.FeedRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock FeedRepository feedRepository;
    @Mock FeedMapper feedMapper;
    @Mock UserRepository userRepository;
    @Mock WeatherRepository weatherRepository;

    @InjectMocks FeedService feedService;

    @Captor ArgumentCaptor<Feed> feedCaptor;

    UUID authorId;
    UUID weatherId;
    UUID c1; UUID c2;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        weatherId = UUID.randomUUID();
        c1 = UUID.randomUUID();
        c2 = UUID.randomUUID();
    }

    @Test
    void 피드_등록_성공시_날씨가_연결되고_중복된_의상ID가_제거된다() {
        // given
        var author = mock(User.class);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));

        var weather = mock(Weather.class);
        when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(weather));

        when(feedRepository.save(any(Feed.class))).thenAnswer(inv -> inv.getArgument(0));
        when(feedMapper.toResponse(any(Feed.class))).thenReturn(mock(FeedResponse.class));

        var req = new FeedCreateRequest(
            authorId,
            weatherId,
            List.of(c1, c2, c1),
            "hello"
        );

        // when
        FeedResponse res = feedService.create(req);

        // then
        assertThat(res).isNotNull();

        verify(feedRepository).save(feedCaptor.capture());
        Feed saved = feedCaptor.getValue();

        List<FeedClothes> links = saved.getFeedClothes();
        assertThat(links).hasSize(2);
        assertThat(links.stream().map(FeedClothes::getClothesId)).containsExactlyInAnyOrder(c1, c2);

        assertThat(saved.getLikeCount()).isZero();
        assertThat(saved.getCommentCount()).isZero();

        verify(feedMapper).toResponse(any(Feed.class));
    }

    @Test
    void 피드_등록_성공시_날씨가_없어도_저장된다() {
        // given
        when(userRepository.findById(authorId)).thenReturn(Optional.of(mock(User.class)));
        when(feedRepository.save(any(Feed.class))).thenAnswer(inv -> inv.getArgument(0));
        when(feedMapper.toResponse(any(Feed.class))).thenReturn(mock(FeedResponse.class));

        var req = new FeedCreateRequest(
            authorId,
            null,
            List.of(),
            "no weather"
        );

        // when
        FeedResponse res = feedService.create(req);

        // then
        assertThat(res).isNotNull();
        verify(weatherRepository, never()).findById(any());
        verify(feedRepository).save(any(Feed.class));
    }

    @Test
    void 피드_등록시_작성자를_찾지_못하면_실패한다() {
        //given
        var authorId = UUID.randomUUID();
        when(userRepository.findById(authorId)).thenReturn(Optional.empty());
        var req = new FeedCreateRequest(authorId, null, List.of(UUID.randomUUID()), "x");

        // when & then
        assertThatThrownBy(() -> feedService.create(req))
            .isInstanceOf(GlobalException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verifyNoInteractions(feedRepository, feedMapper, weatherRepository);
    }

    @Test
    void 피드_등록시_날씨를_찾지_못하면_실패한다() {
        // given
        var authorId = UUID.randomUUID();
        var weatherId = UUID.randomUUID();
        when(userRepository.findById(authorId)).thenReturn(Optional.of(mock(User.class)));
        when(weatherRepository.findById(weatherId)).thenReturn(Optional.empty());

        var req = new FeedCreateRequest(authorId, weatherId, List.of(UUID.randomUUID()), "x");

        // when & then
        assertThatThrownBy(() -> feedService.create(req))
            .isInstanceOf(GlobalException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WEATHER_NOT_FOUND);

        verify(feedRepository, never()).save(any());
        verify(feedMapper, never()).toResponse(any());
    }
}
