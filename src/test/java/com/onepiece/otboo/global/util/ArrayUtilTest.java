package com.onepiece.otboo.global.util;

import static org.junit.jupiter.api.Assertions.*;

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
}