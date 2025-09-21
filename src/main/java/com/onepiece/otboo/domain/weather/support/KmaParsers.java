package com.onepiece.otboo.domain.weather.support;

import com.onepiece.otboo.global.util.NumberStrings;
import com.onepiece.otboo.infra.api.dto.KmaItem;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KmaParsers {

    // PCP: 코드(1/2/3) 또는 문자열("1~4mm", "50mm이상", "강수없음") -> mm/h 하한값
    public static Double parsePcpAmountOrCode(KmaItem it) {
        if (it == null || it.fcstValue() == null) {
            return 0.0;
        }
        String v = it.fcstValue().trim();

        // 코드값 처리
        if (NumberStrings.isPureInt(v)) {
            int code = Integer.parseInt(v);
            return switch (code) {
                case 1 -> 0.1;  // 약한 비: <3mm/h → 0.1 (0은 '강수없음'과 구분)
                case 2 -> 3.0;  // 보통 비: 3~15 → 3
                case 3 -> 15.0; // 강한 비: ≥15 → 15
                default -> 0.0;
            };
        }

        if (v.contains("강수없음")) {
            return 0.0;
        }

        Matcher mRange = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*~").matcher(v); // "1~4mm"
        if (mRange.find()) {
            return Double.valueOf(mRange.group(1));
        }

        Matcher mNum = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(v); // "50mm 이상", "3mm"
        if (mNum.find()) {
            return Double.valueOf(mNum.group(1));
        }

        return null;
    }

    public static Double parseWsdToDouble(KmaItem wsdItem) {
        if (wsdItem == null || wsdItem.fcstValue() == null) {
            return null;
        }
        String v = wsdItem.fcstValue().trim();

        if (NumberStrings.isPureInt(v)) {
            int code = Integer.parseInt(v);
            return switch (code) {
                case 1 -> 4.0; // 약한 바람: ≥4 → 4.0 (대표값 조정 가능)
                case 2 -> 4.0; // 약간 강한 바람: 4~9 → 4.0
                case 3 -> 9.0; // 강한 바람: ≥9 → 9.0
                default -> null;
            };
        }

        try {
            return Double.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double resolveTmx(Map<String, KmaItem> bucket, Extremes ex, String date) {
        // 버킷에서 직접 찾아본다
        Double tmx = toDouble(bucket.get("TMX"));

        if (tmx != null) {
            return tmx;
        }

        // 없으면 Extremes에서 대체
        return ex.tmxByDate().get(date);
    }

    public static Double resolveTmn(Map<String, KmaItem> bucket, Extremes ex, String date) {
        // 1. 버킷에서 직접 찾아본다
        Double tmn = toDouble(bucket.get("TMN"));
        if (tmn != null) {
            return tmn;
        }
        // 2. 없으면 Extremes에서 대체
        return ex.tmnByDate().get(date);
    }

    public static Double toDouble(KmaItem it) {
        if (it == null || it.fcstValue() == null) {
            return null;
        }
        try {
            return Double.valueOf(it.fcstValue());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
