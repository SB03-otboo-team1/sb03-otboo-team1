package com.onepiece.otboo.domain.profile.fixture;

import com.onepiece.otboo.domain.location.entity.Location;
import com.onepiece.otboo.domain.location.fixture.LocationFixture;
import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.enums.Gender;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import java.time.LocalDate;

public class ProfileFixture {

    public static Profile createProfile() {
        return Profile.builder()
            .nickname("테스트닉네임")
            .profileImageUrl("https://example.com/image.png")
            .gender(Gender.MALE)
            .birthDate(LocalDate.of(2000, 1, 1))
            .tempSensitivity(5)
            .user(UserFixture.createUser())
            .location(null)
            .build();
    }

    public static Profile createProfile(User user) {
        return Profile.builder()
            .nickname("test")
            .profileImageUrl("https://example.com/image.png")
            .gender(Gender.MALE)
            .birthDate(LocalDate.of(1999, 7, 2))
            .tempSensitivity(5)
            .user(user)
            .location(LocationFixture.createLocation())
            .build();
    }

    public static Profile createProfile(User user, String name, String profileImageUrl,
        Gender gender, LocalDate birthDate, Integer tempSensitivity, Location location) {
        return Profile.builder()
            .nickname(name)
            .profileImageUrl(profileImageUrl)
            .gender(gender)
            .birthDate(birthDate)
            .tempSensitivity(tempSensitivity)
            .user(user)
            .location(location)
            .build();
    }
}
