package com.onepiece.otboo.domain.user.dto.request;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;

import com.onepiece.otboo.domain.user.enums.Role;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserGetRequestTest {

    @Test
    void 기본값_적용_테스트() {

        // when
        UserGetRequest request = UserGetRequest.builder().build();

        // then
        assertEquals(20, request.limit());
        assertEquals("createdAt", request.sortBy());
        assertEquals("DESCENDING", request.sortDirection());
    }

    @Test
    void builder로_값을_주면_그대로_반영된다() {
        
        // given
        UUID id = UUID.randomUUID();

        // when
        UserGetRequest request = UserGetRequest.builder()
            .cursor("cursor123")
            .idAfter(id)
            .limit(50)
            .sortBy("email")
            .sortDirection("ASCENDING")
            .emailLike("test")
            .roleEqual(Role.USER)
            .locked(true)
            .build();

        // then
        assertEquals("cursor123", request.cursor());
        assertEquals(id, request.idAfter());
        assertEquals(50, request.limit());
        assertEquals("email", request.sortBy());
        assertEquals("ASCENDING", request.sortDirection());
        assertEquals("test", request.emailLike());
        assertEquals(Role.USER, request.roleEqual());
        assertTrue(request.locked());

    }

    @Test
    void sortBy관련_헬퍼메서드_검증() {
        UserGetRequest request1 = UserGetRequest.builder().sortBy("createdAt").build();
        assertTrue(request1.sortByCreatedAt());
        assertEquals(UserGetRequest.SortBy.CREATED_AT, request1.sortByEnum());

        UserGetRequest request2 = UserGetRequest.builder().sortBy("email").build();
        assertFalse(request2.sortByCreatedAt());
        assertEquals(UserGetRequest.SortBy.EMAIL, request2.sortByEnum());
    }

    @Test
    void sortDirection관련_헬퍼메서드_검증() {
        UserGetRequest asc = UserGetRequest.builder().sortDirection("ASCENDING").build();
        assertTrue(asc.isAscending());

        UserGetRequest desc = UserGetRequest.builder().sortDirection("DESCENDING").build();
        assertFalse(desc.isAscending());
    }

    @Test
    void createdAt_문자열이_올바르면_Instant로_파싱된다() {

        // given
        String iso = "2025-09-22T12:34:56Z";

        // when
        Instant instant = UserGetRequest.parseCreatedAtStrict(iso);

        // then
        assertEquals(iso ,instant.toString());
    }

    @Test
    void createdAt_문자열이_잘못되면_예외발생() {

        // given
        String invalid = "not-a-date";

        // when
        Throwable thrown = catchThrowable(() -> UserGetRequest.parseCreatedAtStrict(invalid));

        // then
        assertThat(thrown)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("createdAt 커서가 유효한 ISO-8601 형식이 아닙니다.");
    }
}