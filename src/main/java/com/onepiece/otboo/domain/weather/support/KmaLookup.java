package com.onepiece.otboo.domain.weather.support;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class KmaLookup {

    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

    /**
     * 같은 HHmm가 없으면 ±1시간 → ±2시간까지 근접 시각을 탐색
     */
    public static Double lookupSameOrNearest(
        Map<ForecastKey, Double> index,
        LocalDate prevDate,
        String timeKey
    ) {
        // 1. 동일 시각 우선 탐색
        ForecastKey exact = new ForecastKey(prevDate.format(DATE), timeKey);
        if (index.containsKey(exact)) {
            return index.get(exact);
        }

        // 2. ±1, ±2 시간까지 탐색
        int hh = Integer.parseInt(timeKey.substring(0, 2));
        int mm = Integer.parseInt(timeKey.substring(2, 4));

        int[] offsets = {-1, 1, -2, 2};
        for (int off : offsets) {
            int nh = hh + off;
            if (nh < 0 || nh > 23) continue; // 시간 범위 넘어가면 skip

            String neighborTime = String.format("%02d%02d", nh, mm);
            ForecastKey neighborKey = new ForecastKey(
                prevDate.format(DATE), neighborTime
            );

            if (index.containsKey(neighborKey)) {
                return index.get(neighborKey);
            }
        }

        // 3. 못 찾으면 null
        return null;
    }
}
