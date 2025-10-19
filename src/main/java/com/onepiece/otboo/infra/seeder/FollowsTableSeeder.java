package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.randInt;

import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.follow.repository.FollowRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(130)
@RequiredArgsConstructor
public class FollowsTableSeeder implements DataSeeder {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    public void seed() {
        if (followRepository.count() > 0) {
            return;
        }

        List<User> users = userRepository.findAll();
        if (users.size() < 2) {
            return;
        }

        Set<String> used = new HashSet<>();
        int created = 0;
        while (created < 10 && used.size() < users.size() * users.size()) {
            User follower = users.get(randInt(0, users.size() - 1));
            User following = users.get(randInt(0, users.size() - 1));
            if (follower.getId().equals(following.getId())) {
                continue; // 자기 자신을 팔로우할 수 없음
            }
            String key = follower.getId() + ":" + following.getId();
            if (used.add(key)) {
                Follow follow = Follow.builder()
                    .follower(follower)
                    .following(following)
                    .build();
                followRepository.save(follow);
                created++;
            }
        }
        log.info("FollowsTableSeeder: {}개의 팔로우 더미 데이터가 추가되었습니다.", created);
    }
}
