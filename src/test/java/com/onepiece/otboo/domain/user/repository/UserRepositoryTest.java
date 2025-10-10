package com.onepiece.otboo.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.onepiece.otboo.domain.profile.entity.Profile;
import com.onepiece.otboo.domain.profile.repository.ProfileRepository;
import com.onepiece.otboo.domain.user.dto.request.UserGetRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.enums.Role;
import com.onepiece.otboo.domain.user.fixture.UserFixture;
import com.onepiece.otboo.global.config.TestJpaConfig;
import com.onepiece.otboo.global.config.TestJpaConfig.MutableDateTimeProvider;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private MutableDateTimeProvider time;

    private UUID id1, id2, id3;

    @BeforeEach
    void setUp() {
        time.setNow(Instant.parse("2025-09-22T08:00:00Z"));
        User user1 = UserFixture.createUser("han@test.com");
        userRepository.save(user1);

        time.setNow(Instant.parse("2025-09-22T09:00:00Z"));
        User user2 = UserFixture.createUser("shin@test.com");
        userRepository.save(user2);

        time.setNow(Instant.parse("2025-09-22T10:00:00Z"));
        User user3 = UserFixture.createUser("kim@test.com");
        userRepository.save(user3);

        id1 = user1.getId();
        id2 = user2.getId();
        id3 = user3.getId();

        Profile profile1 = Profile.builder()
            .user(user1)
            .nickname("han")
            .build();
        Profile profile2 = Profile.builder()
            .user(user2)
            .nickname("shin")
            .build();
        Profile profile3 = Profile.builder()
            .user(user3)
            .nickname("kim")
            .build();

        profileRepository.saveAll(List.of(profile1, profile2, profile3));
    }

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

    @Test
    void limit보다_한개_더_많이_조회한다() {

        // given
        UserGetRequest request = UserGetRequest.builder()
            .limit(2)
            .sortBy(SortBy.CREATED_AT)
            .sortDirection(SortDirection.ASCENDING)
            .emailLike("test.com")
            .build();

        // when
        List<UserDto> result = userRepository.findUsers(request);

        // then
        assertEquals(3, result.size());
        assertEquals("han@test.com", result.get(0).email());
        assertEquals("shin@test.com", result.get(1).email());
        assertEquals("kim@test.com", result.get(2).email());
    }

    @ParameterizedTest(name = "sortBy = {0}, sortDirection = {1}")
    @CsvSource({
        "EMAIL,ASCENDING",
        "EMAIL,DESCENDING",
        "CREATED_AT,ASCENDING",
        "CREATED_AT,DESCENDING"
    })
    void 두번째_페이지_커서_조회_테스트(SortBy sortBy, SortDirection sortDirection) {

        // given
        UserGetRequest first = UserGetRequest.builder()
            .limit(2)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();
        List<UserDto> firstRows = userRepository.findUsers(first);
        UserDto lastOfPage1 = firstRows.get(1);

        String cursor = SortBy.EMAIL.equals(sortBy)
            ? lastOfPage1.email()
            : lastOfPage1.createdAt().toString();

        UserGetRequest second = UserGetRequest.builder()
            .limit(2)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .cursor(cursor)
            .idAfter(lastOfPage1.id())
            .build();

        // when
        List<UserDto> secondRows = userRepository.findUsers(second);

        // then
        assertEquals(1, secondRows.size());
        String expectedEmail;
        UUID expectedId;

        if (SortBy.EMAIL.equals(sortBy) && SortDirection.ASCENDING.equals(sortDirection)) {
            // 1페이지: han, kim → 2페이지: shin
            expectedEmail = "shin@test.com";
            expectedId = id2;
        } else if (SortBy.EMAIL.equals(sortBy) && SortDirection.DESCENDING.equals(sortDirection)) {
            expectedEmail = "han@test.com";
            expectedId = id1;
        } else if (SortBy.CREATED_AT.equals(sortBy) && SortDirection.ASCENDING.equals(
            sortDirection)) {
            expectedEmail = "kim@test.com";
            expectedId = id3;
        } else {
            expectedEmail = "han@test.com";
            expectedId = id1;
        }

        UserDto only = secondRows.get(0);
        assertEquals(expectedEmail, only.email());
        assertEquals(expectedId, only.id());

        // 정렬/커서 일관성 확인
        if (SortBy.EMAIL.equals(sortBy)) {
            if (SortDirection.ASCENDING.equals(sortDirection)) {
                // 다음 페이지 값이 이전 마지막보다 커야 함
                assertTrue(only.email().compareTo(lastOfPage1.email()) > 0);
            } else {
                assertTrue(only.email().compareTo(lastOfPage1.email()) < 0);
            }
        } else { // createdAt
            if (SortDirection.ASCENDING.equals(sortDirection)) {
                assertFalse(only.createdAt().isBefore(lastOfPage1.createdAt()));
            } else {
                assertFalse(only.createdAt().isAfter(lastOfPage1.createdAt()));
            }
        }
    }

    @Test
    void 조건에_맞는_데이터_개수_조회_테스트() {

        // given
        String emailLike = "n@";
        Role role = Role.USER;
        Boolean locked = false;

        // when
        Long count = userRepository.countUsers(emailLike, role, locked);

        // then
        assertEquals(2L, count);
    }

    @AfterEach
    void tearDown() {
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }
}
