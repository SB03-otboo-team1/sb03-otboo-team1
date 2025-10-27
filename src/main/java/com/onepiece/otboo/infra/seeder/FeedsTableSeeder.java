package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.randStr;

import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.feed.repository.FeedRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(90)
@RequiredArgsConstructor
public class FeedsTableSeeder implements DataSeeder {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    @Override
    public void seed() {
        if (feedRepository.count() > 0) {
            return;
        }

        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            return;
        }

        for (int i = 1; i <= 10; i++) {
            Feed feed = Feed.builder()
                .authorId(users.get((i - 1) % users.size()).getId())
                .content("Feed content " + i + " - " + randStr(12))
                .likeCount(0)
                .commentCount(0)
                .build();
            feedRepository.save(feed);
        }
        log.info("FeedsTableSeeder: 10개의 피드 더미 데이터가 추가되었습니다.");
    }
}
