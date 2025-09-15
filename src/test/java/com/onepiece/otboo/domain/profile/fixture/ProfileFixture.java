package com.onepiece.otboo.domain.profile.fixture;

import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.enums.Gender;
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
}
