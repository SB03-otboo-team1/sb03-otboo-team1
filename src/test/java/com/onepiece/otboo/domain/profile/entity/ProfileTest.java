package com.onepiece.otboo.domain.profile.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.onepiece.otboo.domain.profile.enums.Gender;
import com.onepiece.otboo.domain.profile.fixture.ProfileFixture;
import org.junit.jupiter.api.Test;

public class ProfileTest {

    @Test
    void profile_객체_생성() {
        Profile profile = ProfileFixture.createProfile();
        assertThat(profile.getNickname()).isEqualTo("테스트닉네임");
        assertThat(profile.getGender()).isEqualTo(Gender.MALE);
        assertThat(profile.getProfileImageUrl()).isEqualTo("https://example.com/image.png");
        assertThat(profile.getBirthDate()).isNotNull();
        assertThat(profile.getTempSensitivity()).isEqualTo(5);
        assertThat(profile.getUser()).isNotNull();
    }

    @Test
    void 닉네임_변경_테스트() {
        Profile profile = ProfileFixture.createProfile();
        profile.updateNickname("새닉네임");
        assertThat(profile.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    void 성별_변경_테스트() {
        Profile profile = ProfileFixture.createProfile();
        profile.updateGender(Gender.FEMALE);
        assertThat(profile.getGender()).isEqualTo(Gender.FEMALE);
    }
}
