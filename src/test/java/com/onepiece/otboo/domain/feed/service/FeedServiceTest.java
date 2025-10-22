package com.onepiece.otboo.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.onepiece.otboo.domain.clothes.entity.Clothes;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.repository.ClothesRepository;
import com.onepiece.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.feed.entity.FeedClothes;
import com.onepiece.otboo.domain.feed.mapper.FeedMapper;
import com.onepiece.otboo.domain.feed.repository.FeedLikeRepository;
import com.onepiece.otboo.domain.feed.repository.FeedRepository;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.mapper.WeatherMapper;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    FeedRepository feedRepository;
    @Mock
    FeedMapper feedMapper;
    @Mock
    UserRepository userRepository;
    @Mock
    WeatherRepository weatherRepository;
    @Mock
    ClothesRepository clothesRepository;
    @Mock
    ProfileRepository profileRepository;
    @Mock
    WeatherMapper weatherMapper;
    @Mock
    FeedLikeRepository feedLikeRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    FeedService feedService;

    @InjectMocks
    FeedLikeService feedLikeService;


    @Captor
    ArgumentCaptor<Feed> feedCaptor;

    UUID authorId;
    UUID weatherId;
    UUID c1;
    UUID c2;

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
        var profile = mock(com.onepiece.otboo.domain.profile.entity.Profile.class);

        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(profileRepository.findByUserId(authorId)).thenReturn(Optional.of(profile));
        when(profile.getNickname()).thenReturn("nickname");
        when(profile.getProfileImageUrl()).thenReturn("img.jpg");

        var weather = mock(Weather.class);
        when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(weather));
        when(weather.getId()).thenReturn(weatherId);
        when(weather.getSkyStatus()).thenReturn(null);

        when(weatherMapper.toPrecipitationDto(any())).thenReturn(null);
        when(weatherMapper.toTemperatureDto(any())).thenReturn(null);
        when(author.getId()).thenReturn(authorId);

        var clothes1 = mock(Clothes.class);
        var clothes2 = mock(Clothes.class);
        when(clothes1.getId()).thenReturn(c1);
        when(clothes2.getId()).thenReturn(c2);
        when(clothes1.getOwner()).thenReturn(author);
        when(clothes2.getOwner()).thenReturn(author);
        when(clothes1.getName()).thenReturn("상의");
        when(clothes2.getName()).thenReturn("하의");
        when(clothes1.getImageUrl()).thenReturn("1.jpg");
        when(clothes2.getImageUrl()).thenReturn("2.jpg");
        when(clothes1.getType()).thenReturn(ClothesType.TOP);
        when(clothes2.getType()).thenReturn(ClothesType.BOTTOM);

        when(clothesRepository.findAllById(argThat(ids -> {
            List<UUID> idList = new ArrayList<>();
            ids.forEach(idList::add);
            return idList.containsAll(List.of(c1, c2)) && idList.size() == 2;
        }))).thenReturn(List.of(clothes1, clothes2));

        when(feedRepository.save(any(Feed.class))).thenAnswer(inv -> inv.getArgument(0));
        when(feedMapper.toResponse(any(), any(), any(), any(), anyBoolean())).thenReturn(
            mock(FeedResponse.class));

        var req = new FeedCreateRequest(
            authorId,
            weatherId,
            List.of(c1, c2, c1),
            "hello"
        );

        // when
        FeedResponse res = feedService.create(req);

        // then 이하 동일
        assertThat(res).isNotNull();
        verify(feedRepository).save(feedCaptor.capture());
        Feed saved = feedCaptor.getValue();
        List<FeedClothes> links = saved.getFeedClothes();
        assertThat(links).hasSize(2);
        assertThat(links.stream().map(FeedClothes::getClothesId)).containsExactlyInAnyOrder(c1, c2);
        assertThat(saved.getLikeCount()).isZero();
        assertThat(saved.getCommentCount()).isZero();
        verify(feedMapper).toResponse(any(), any(), any(), any(), anyBoolean());
    }


    @Test
    void 피드_등록_성공시_날씨가_없어도_저장된다() {
        // given
        var author = mock(User.class);
        var profile = mock(com.onepiece.otboo.domain.profile.entity.Profile.class);

        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(profileRepository.findByUserId(authorId)).thenReturn(Optional.of(profile));
        when(profile.getNickname()).thenReturn("nickname");
        when(profile.getProfileImageUrl()).thenReturn("img.jpg");

        when(clothesRepository.findAllById(any())).thenReturn(List.of());
        when(feedRepository.save(any(Feed.class))).thenAnswer(inv -> inv.getArgument(0));
        when(feedMapper.toResponse(any(), any(), any(), any(), anyBoolean())).thenReturn(
            mock(FeedResponse.class));

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
            .isInstanceOf(com.onepiece.otboo.global.exception.GlobalException.class)
            .extracting("errorCode")
            .isEqualTo(com.onepiece.otboo.global.exception.ErrorCode.USER_NOT_FOUND);

        verifyNoInteractions(feedRepository, feedMapper, weatherRepository, profileRepository,
            clothesRepository, weatherMapper);
    }

    @Test
    void 피드_등록시_날씨를_찾지_못하면_실패한다() {
        // given
        var authorId = UUID.randomUUID();
        var weatherId = UUID.randomUUID();
        var author = mock(User.class);
        var profile = mock(Profile.class);

        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(profileRepository.findByUserId(authorId)).thenReturn(Optional.of(profile));
        when(profile.getNickname()).thenReturn("nickname");
        when(profile.getProfileImageUrl()).thenReturn("img.jpg");
        when(weatherRepository.findById(weatherId)).thenReturn(Optional.empty());

        var req = new FeedCreateRequest(authorId, weatherId, List.of(UUID.randomUUID()), "x");

        // when & then
        assertThatThrownBy(() -> feedService.create(req))
            .isInstanceOf(com.onepiece.otboo.global.exception.GlobalException.class)
            .extracting("errorCode")
            .isEqualTo(com.onepiece.otboo.global.exception.ErrorCode.WEATHER_NOT_FOUND);

        verify(feedRepository, never()).save(any());
        verify(feedMapper, never()).toResponse(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void 피드_삭제_소유자_요청시_정상삭제() {
        UUID feedId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Feed feed = mock(Feed.class);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feed.getAuthorId()).thenReturn(ownerId);

        feedService.delete(feedId, ownerId);

        ArgumentCaptor<Feed> captor = ArgumentCaptor.forClass(Feed.class);
        verify(feedRepository).delete(captor.capture());
    }

    @Test
    void 피드_삭제_타인소유_피드_삭제시_실패() {
        // given
        UUID feedId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        Feed feed = mock(Feed.class);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feed.getAuthorId()).thenReturn(ownerId);

        // when & then
        assertThatExceptionOfType(GlobalException.class)
            .isThrownBy(() -> feedService.delete(feedId, requesterId))
            .matches(ex -> ((GlobalException) ex).getErrorCode() == ErrorCode.FEED_FORBIDDEN);
    }

    @Test
    void 피드_삭제_존재하지_않는_피드_삭제시_실패() {
        // given
        UUID feedId = UUID.randomUUID();
        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(GlobalException.class)
            .isThrownBy(() -> feedService.delete(feedId, UUID.randomUUID()))
            .matches(ex -> ((GlobalException) ex).getErrorCode() == ErrorCode.FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("좋아요 등록 시 FeedLikedEvent가 발행된다")
    void like_성공시_이벤트_발행() {
        UUID userId = UUID.randomUUID();
        UUID feedId = UUID.randomUUID();

        Feed feed = mock(Feed.class);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feed.getAuthorId()).thenReturn(UUID.randomUUID());
        when(profileRepository.findByUserId(any())).thenReturn(Optional.of(mock(Profile.class)));

        feedLikeService.like(userId, feedId);

        verify(feedLikeRepository).save(any());
        verify(eventPublisher).publishEvent(any());
    }
}
