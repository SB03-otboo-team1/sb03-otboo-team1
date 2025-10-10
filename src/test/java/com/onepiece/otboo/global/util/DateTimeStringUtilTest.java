package com.onepiece.otboo.global.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class DateTimeStringUtilTest {

    @Test
    void 날짜_문자열_정규화_테스트() {
        assertEquals("20250102", DateTimeStringUtil.normalizeDate("2025-01-02T12:34:56+0900"));
        assertEquals("20250102", DateTimeStringUtil.normalizeDate("20250102"));
        assertEquals("20250102", DateTimeStringUtil.normalizeDate("2025/01/02"));
    }

    @Test
    void yyyyMMdd_보다_짧은_입력은_가능한_숫자까지만_반환한다() {
        assertEquals("2025", DateTimeStringUtil.normalizeDate("2025"));
        assertEquals("202501", DateTimeStringUtil.normalizeDate("2025-01"));
    }

    @Test
    void 날짜_문자열_정규화_null_반환_테스트() {
        assertNull(DateTimeStringUtil.normalizeDate(null));
    }

    @Test
    void 시간_문자열_패딩_테스트() {
        assertEquals("0001", DateTimeStringUtil.padHHmm("1"));
        assertEquals("0012", DateTimeStringUtil.padHHmm("12"));
        assertEquals("0123", DateTimeStringUtil.padHHmm("123"));
        assertEquals("1234", DateTimeStringUtil.padHHmm("1234"));
        assertEquals("1234", DateTimeStringUtil.padHHmm("123456")); // 앞 4자리만
    }

    @Test
    void 시간_문자열에서_숫자_외_문자를_제거하고_패딩한다() {
        assertEquals("0930", DateTimeStringUtil.padHHmm("09:30"));
        assertEquals("1234", DateTimeStringUtil.padHHmm("12:34:56"));
    }

    @Test
    void 시간_문자열_패딩_null_반환_테스트() {
        assertNull(DateTimeStringUtil.padHHmm(null));
    }
}
