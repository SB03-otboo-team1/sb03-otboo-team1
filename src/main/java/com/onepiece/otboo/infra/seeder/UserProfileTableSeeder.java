package com.onepiece.otboo.infra.seeder;

import static com.onepiece.otboo.infra.seeder.SeedUtils.randInt;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.repository.LocationRepository;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.enums.Gender;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(30)
@RequiredArgsConstructor
public class UserProfileTableSeeder implements DataSeeder {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    @Override
    public void seed() {
        if (profileRepository.count() > 0) {
            return;
        }
        // 모든 유저, 위치 id 조회
        List<User> users = userRepository.findAll();
        List<Location> locations = locationRepository.findAll();
        if (users.isEmpty()) {
            return;
        }
        int count = 0;
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Location location =
                locations.isEmpty() ? null : locations.get(randInt(0, locations.size() - 1));
            String nickname = "user" + (i + 1);
            Gender gender = switch (i % 3) {
                case 0 -> Gender.MALE;
                case 1 -> Gender.FEMALE;
                default -> Gender.OTHER;
            };
            Integer tempSensitivity = randInt(1, 5);
            Profile profile = Profile.builder()
                .user(user)
                .location(location)
                .nickname(nickname)
                .gender(gender)
                .tempSensitivity(tempSensitivity)
                .build();
            profileRepository.save(profile);
            count++;
        }
        log.info("UserProfileTableSeeder: {}개의 프로필 더미 데이터가 추가되었습니다.", count);
    }
}
