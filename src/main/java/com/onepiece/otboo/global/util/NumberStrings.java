package com.onepiece.otboo.global.util;

public class NumberStrings {

    public static boolean isPureInt(String v) {
        return v != null && v.trim().matches("\\d+");
    }
}
