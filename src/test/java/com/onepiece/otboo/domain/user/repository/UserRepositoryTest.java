package com.onepiece.otboo.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import com.onepiece.otboo.global.config.TestJpaConfig;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 이메일로_사용자_조회_성공() {
        // given
        User user = UserFixture.createUser();
        userRepository.save(user);

        // when
        Optional<User> result = userRepository.findByEmail(user.getEmail());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
    }
}
