package com.onepiece.otboo.global.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberConverter {

    /**
     * 지정된 소수점 자리까지 반올림
     *
     * @param value 반올림할 값
     * @param scale 소수점 자리
     * @return 반올림된 double 값
     */
    public static double round(double value, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("scale은 0보다 커야합니다.");
        }

        BigDecimal decimal = BigDecimal.valueOf(value);

        return decimal.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
}
