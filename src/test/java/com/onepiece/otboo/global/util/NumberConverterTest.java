package com.onepiece.otboo.global.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NumberConverterTest {

    @Test
    void double_타입_숫자_반올림_테스트() {

        // given
        double num = 123.45678;

        // when
        double roundedNum = NumberConverter.round(num, 3);

        // then
        assertEquals(123.457, roundedNum);
    }
}