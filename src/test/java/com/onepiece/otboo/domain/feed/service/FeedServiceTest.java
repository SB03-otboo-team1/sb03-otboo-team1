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
    void create_success_withWeather_and_dedupApplied() {
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

        FeedResponse res = feedService.create(req);

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
    void create_success_withoutWeather() {
        when(userRepository.findById(authorId)).thenReturn(Optional.of(mock(User.class)));
        when(feedRepository.save(any(Feed.class))).thenAnswer(inv -> inv.getArgument(0));
        when(feedMapper.toResponse(any(Feed.class))).thenReturn(mock(FeedResponse.class));

        var req = new FeedCreateRequest(
            authorId,
            null,
            List.of(),
            "no weather"
        );

        FeedResponse res = feedService.create(req);

        assertThat(res).isNotNull();
        verify(weatherRepository, never()).findById(any());
        verify(feedRepository).save(any(Feed.class));
    }

    @Test
    void create_fail_authorNotFound() {
        when(userRepository.findById(authorId)).thenReturn(Optional.empty());
        var req = new FeedCreateRequest(authorId, null, List.of(c1), "x");

        assertThatThrownBy(() -> feedService.create(req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("author not found");

        verify(feedRepository, never()).save(any());
        verify(feedMapper, never()).toResponse(any());
    }

    @Test
    void create_fail_weatherNotFound() {
        when(userRepository.findById(authorId)).thenReturn(Optional.of(mock(User.class)));
        when(weatherRepository.findById(weatherId)).thenReturn(Optional.empty());

        var req = new FeedCreateRequest(authorId, weatherId, List.of(c1), "x");

        assertThatThrownBy(() -> feedService.create(req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("weather not found");

        verify(feedRepository, never()).save(any());
        verify(feedMapper, never()).toResponse(any());
    }
}
