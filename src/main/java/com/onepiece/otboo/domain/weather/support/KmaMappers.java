package com.onepiece.otboo.domain.weather.support;

import com.onepiece.otboo.domain.weather.enums.PrecipitationType;
import com.onepiece.otboo.domain.weather.enums.SkyStatus;
import com.onepiece.otboo.domain.weather.enums.WindSpeedWord;
import com.onepiece.otboo.global.util.NumberStrings;
import com.onepiece.otboo.infra.api.dto.KmaItem;

public class KmaMappers {

    /**
     * WSD → WindSpeedWord 매핑 - 코드값: 1=WEAK, 2=MODERATE, 3=STRONG - 숫자(m/s): <4 → WEAK, 4~<9 →
     * MODERATE, >=9 → STRONG
     */
    public static WindSpeedWord mapWindSpeedWord(KmaItem wsdItem) {
        if (wsdItem == null || wsdItem.fcstValue() == null) {
            return null;
        }
        String v = wsdItem.fcstValue().trim();

        if (NumberStrings.isPureInt(v)) {
            return switch (Integer.parseInt(v)) {
                case 1 -> WindSpeedWord.WEAK;
                case 2 -> WindSpeedWord.MODERATE;
                case 3 -> WindSpeedWord.STRONG;
                default -> null;
            };
        }

        Double speed = KmaParsers.toDouble(wsdItem);
        if (speed == null) {
            return null;
        }
        if (speed < 4.0) {
            return WindSpeedWord.WEAK;
        }
        if (speed < 9.0) {
            return WindSpeedWord.MODERATE;
        }
        return WindSpeedWord.STRONG;
    }

    public static SkyStatus mapSky(KmaItem it) {
        if (it == null) {
            return null;
        }
        try {
            int code = Integer.parseInt(it.fcstValue());
            // 1 맑음, 3 구름많음, 4 흐림
            return switch (code) {
                case 1 -> SkyStatus.CLEAR;
                case 3 -> SkyStatus.MOSTLY_CLOUDY;
                case 4 -> SkyStatus.CLOUDY;
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    public static PrecipitationType mapPty(KmaItem it) {
        if (it == null) {
            return null;
        }
        try {
            int code = Integer.parseInt(it.fcstValue());
            // 0 없음, 1 비, 2 비/눈, 3 눈, 4 소나기, (5~7: 빗방울/눈날림 등은 필요시 확장)
            return switch (code) {
                case 0 -> PrecipitationType.NONE;
                case 1 -> PrecipitationType.RAIN;
                case 2 -> PrecipitationType.RAIN_SNOW;
                case 3 -> PrecipitationType.SNOW;
                case 4 -> PrecipitationType.SHOWER;
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }
}
