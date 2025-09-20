package com.onepiece.otboo.global.util;

public class DoubleRoundingConverter {

    public static double roundTo4(double value) {
        return Math.round(value * 10000d) / 10000d;
    }

    public static double roundTo1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
