package com.onepiece.otboo.infra.api.mapper;

import com.onepiece.otboo.global.util.NumberConverter;
import com.onepiece.otboo.infra.api.client.OpenWeatherClient;
import com.onepiece.otboo.infra.api.client.OpenWeatherClient.Item;
import com.onepiece.otboo.infra.api.client.OpenWeatherClient.Root;
import com.onepiece.otboo.infra.api.dto.KmaItem;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OwmToKmaItemMapper {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HHmm");

    /**
     * OWM Root → KmaItem 리스트로 변환 (nx, ny 포함)
     */
    public static List<KmaItem> map(Root root, int nx, int ny) {
        if (root == null || root.list == null || root.list.isEmpty()) {
            return List.of();
        }

        // base(=발표시각) : 현재 KST
        ZonedDateTime now = ZonedDateTime.now(KST);
        String baseDate = now.format(DATE);
        String baseTime = now.format(TIME);

        List<KmaItem> out = new ArrayList<>();

        Map<String, Double> tmxByDate = new HashMap<>();
        Map<String, Double> tmnByDate = new HashMap<>();

        for (Item it : root.list) {
            String[] dt = OpenWeatherClient.fcstDateTimeKST(it.dt);
            String fcstDate = dt[0];
            String fcstTime = dt[1];

            // 1) TMP (기온)
            if (it.main != null && it.main.temp != null) {
                out.add(kmaOf("TMP", fcstDate, fcstTime, it.main.temp, nx, ny, baseDate, baseTime));
            }
            // 2) REH (습도)
            if (it.main != null && it.main.humidity != null) {
                out.add(
                    kmaOf("REH", fcstDate, fcstTime, it.main.humidity, nx, ny, baseDate, baseTime));
            }
            // 3) POP (강수확률, 0~1 → 0~100)
            if (it.pop != null) {
                out.add(
                    kmaOf("POP", fcstDate, fcstTime, it.pop * 100.0, nx, ny, baseDate, baseTime));
            }
            // 4) PCP (3시간 누적 강수/적설 mm 합산)
            double pcp = 0.0;
            if (it.rain != null && it.rain.get("3h") != null) {
                pcp += it.rain.get("3h");
            }
            if (it.snow != null && it.snow.get("3h") != null) {
                pcp += it.snow.get("3h");
            }
            out.add(kmaOf("PCP", fcstDate, fcstTime, pcp, nx, ny, baseDate, baseTime));

            // 5) WSD (풍속, m/s)
            if (it.wind != null && it.wind.speed != null) {
                out.add(
                    kmaOf("WSD", fcstDate, fcstTime, it.wind.speed, nx, ny, baseDate, baseTime));
            }
            // 6) SKY (하늘상태)
            String skyCode = skyFromWeather(it);
            if (skyCode != null) {
                out.add(kmaOf("SKY", fcstDate, fcstTime, skyCode, nx, ny, baseDate, baseTime));
            }
            // 7) PTY (강수형태)
            String ptyCode = ptyFromWeather(it);
            out.add(kmaOf("PTY", fcstDate, fcstTime, ptyCode, nx, ny, baseDate, baseTime));

            if (it.main != null) {
                Double minTmp = it.main.temp_min != null ? it.main.temp_min : it.main.temp;
                Double maxTmp = it.main.temp_max != null ? it.main.temp_max : it.main.temp;

                tmnByDate.merge(fcstDate, minTmp, Math::min);
                tmxByDate.merge(fcstDate, maxTmp, Math::max);
            }
        }

        for (String yyyymmdd : unionKeys(tmxByDate, tmnByDate)) {
            if (tmxByDate.containsKey(yyyymmdd)) {
                out.add(kmaOf("TMX", yyyymmdd, "0000",
                    NumberConverter.round(tmxByDate.get(yyyymmdd), 1), nx, ny,
                    baseDate, baseTime));
            }
            if (tmnByDate.containsKey(yyyymmdd)) {
                out.add(kmaOf("TMN", yyyymmdd, "0000",
                    NumberConverter.round(tmnByDate.get(yyyymmdd), 1), nx, ny,
                    baseDate, baseTime));
            }
        }
        return out;
    }

    private static String skyFromWeather(Item it) {
        if (it.weather == null || it.weather.isEmpty()) {
            return null;
        }
        int id = it.weather.get(0).id;

        return switch (id) {
            case 800 -> "1"; // CLEAR
            case 803, 804 -> "4"; // CLOUDY
            default -> "3"; // 801, 802를 포함한 다른 id 값들은 MOSTLY_CLOUDY
        };
    }

    private static String ptyFromWeather(Item it) {
        if (it.weather == null || it.weather.isEmpty()) {
            return "0";
        }
        int id = it.weather.get(0).id;

        // https://openweathermap.org/weather-conditions 참고
        return switch (id) {
            // Thunderstorm group: 200–232
            case 200, 201, 202, 210, 211, 212, 221, 230, 231, 232 -> "1";

            // Drizzle group: 300–321
            case 300, 301, 302, 310, 311, 312, 313, 314, 321 -> "1";

            // Rain group: 500–531
            case 500, 501, 502, 503, 504 -> "1";
            case 511 -> "2"; // freezing rain
            case 520, 521, 522, 531 -> "4"; // shower rain

            // Snow group: 600–622
            case 600, 601, 602, 620, 621, 622 -> "3";
            case 611, 612, 613, 615, 616 -> "2";

            // Atmosphere group: 701–781 → 없음
            case 701, 711, 721, 731, 741, 751, 761, 762, 771, 781 -> "0";

            // Default
            default -> "0";
        };
    }

    private static Set<String> unionKeys(Map<String, ?> a, Map<String, ?> b) {
        Set<String> s = new HashSet<>(a.keySet());
        s.addAll(b.keySet());
        return s;
    }

    /**
     * KmaItem 생성 (필드 순서에 맞춤)
     */
    private static KmaItem kmaOf(String category, String fcstDate, String fcstTime,
        Object value, int nx, int ny, String baseDate, String baseTime) {
        return new KmaItem(
            category,
            fcstDate,
            fcstTime,
            String.valueOf(value),
            nx,
            ny,
            baseDate,
            baseTime
        );
    }
}
