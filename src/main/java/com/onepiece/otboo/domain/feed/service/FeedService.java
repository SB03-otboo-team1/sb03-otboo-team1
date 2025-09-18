package com.onepiece.otboo.domain.feed.service;

import com.onepiece.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.onepiece.otboo.domain.feed.dto.response.FeedResponse;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.feed.entity.FeedClothes;
import com.onepiece.otboo.domain.feed.mapper.FeedMapper;
import com.onepiece.otboo.domain.feed.repository.FeedRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.domain.weather.dto.response.WeatherSummaryDto;
import com.onepiece.otboo.domain.weather.entity.Weather;
import com.onepiece.otboo.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final FeedMapper feedMapper;

    private final UserRepository userRepository;
    private final WeatherRepository weatherRepository;
//    private final ClothesRepository clothesRepository;

    @Transactional
    public FeedResponse create(FeedCreateRequest r) {
        User authorEntity = userRepository.findById(r.authorId())
            .orElseThrow(() -> new IllegalArgumentException("author not found"));

        WeatherSummaryDto weatherDto = null;
        if (r.weatherId() != null) {
            Weather weather = weatherRepository.findById(r.weatherId())
                .orElseThrow(() -> new IllegalArgumentException("weather not found"));

            weatherDto = new WeatherSummaryDto(
                weather.getId(),
                weather.getSkyStatus(),
                null,
                null
            );
        }

        var dedup = new HashSet<>(r.clothesIds());

        // TODO: Clothes 모듈 연동 후 소유권 검증 로직 복구
        //long owned = clothesRepository.countByIdInAndOwnerId(dedup, r.authorId());
        //if (owned != dedup.size())
        //    throw new IllegalArgumentException("clothes ownership mismatch");

        Feed feed = Feed.builder()
            .authorId(r.authorId())
            .weatherId(r.weatherId())
            .content(r.content())
            .likeCount(0L)
            .commentCount(0L)
            .build();

        for (UUID clothesId : dedup) {
            FeedClothes link = FeedClothes.builder()
                .clothesId(clothesId)
                .build();
            link.setFeed(feed);
            feed.getFeedClothes().add(link);
        }

        Feed saved = feedRepository.save(feed);

        return feedMapper.toResponse(saved);
    }
}
