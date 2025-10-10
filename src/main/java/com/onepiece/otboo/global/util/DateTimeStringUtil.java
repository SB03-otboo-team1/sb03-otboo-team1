package com.onepiece.otboo.global.util;

public class DateTimeStringUtil {

    public static String normalizeDate(String s) {
        // 숫자만 남기고 앞 8자리(yyyyMMdd)만 사용
        if (s == null) {
            return null;
        }
        String digits = s.replaceAll("\\D", ""); // "+0900" 같은 비숫자 제거
        return digits.length() >= 8 ? digits.substring(0, 8) : digits;
    }

    public static String padHHmm(String s) {
        if (s == null) {
            return null;
        }
        String digits = s.replaceAll("\\D", "");
        if (digits.length() == 1) {
            return "000" + digits;
        }
        if (digits.length() == 2) {
            return "00" + digits;
        }
        if (digits.length() == 3) {
            return "0" + digits;
        }
        return digits.substring(0, 4);
    }
}
