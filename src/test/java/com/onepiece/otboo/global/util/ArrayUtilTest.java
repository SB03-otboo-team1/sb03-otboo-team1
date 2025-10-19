package com.onepiece.otboo.global.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArrayUtilTest {

    private String[] arr;

    @BeforeEach
    void setUp() {
        // given
        arr = new String[]{"apple", "banana", "carrot", "deer"};
    }

    @Test
    void 배열에_일치하는_항목이_있을_때_index_반환_테스트() {

        // when
        int index = ArrayUtil.indexOf(arr, "banana");

        // then
        assertEquals(1, index);
    }

    @Test
    void 배열에_일치하는_항목이_없을_때_index_반환_테스트() {

        // when
        int index = ArrayUtil.indexOf(arr, "elephant");

        // then
        assertEquals(-1, index);
    }

    @Test
    void List가_null일_때_빈_문자열_반환_테스트() {

        // when
        String str = ArrayUtil.joinString(null);

        // then
        assertEquals("", str);
    }

    @Test
    void List의_문자열_join_테스트() {

        // given
        List<String> list = List.of("경기도", "시흥시", "은행동");

        // when
        String str = ArrayUtil.joinString(list);

        // then
        assertEquals("경기도,시흥시,은행동", str);
    }
}