package com.onepiece.otboo.domain.weather.fixture;

import com.onepiece.otboo.infra.api.dto.KmaItem;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class KmaItemFixture {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE;

    public static List<KmaItem> buildVillageItemsForFiveDays(int nx, int ny, LocalDate baseDate,
        String baseTime) {
        List<KmaItem> items = new ArrayList<>();
        for (int d = 0; d < 5; d++) {
            LocalDate fcst = baseDate.plusDays(d);
            String fcstDate = fcst.format(YYYYMMDD);

            // 3시간 슬롯
            for (String hhmm : List.of("0200", "0500", "0800", "1100", "1400", "1700", "2000",
                "2300")) {
                // 임의 값들 (배치 로직이 파싱만 하면 되도록 정상 범위 값)
                items.add(new KmaItem("TMP", fcstDate, hhmm, String.valueOf(15 + d), nx, ny,
                    baseDate.format(YYYYMMDD), baseTime));
                items.add(new KmaItem("REH", fcstDate, hhmm, String.valueOf(40 + d), nx, ny,
                    baseDate.format(YYYYMMDD), baseTime));
                items.add(new KmaItem("PTY", fcstDate, hhmm, "0", nx, ny, baseDate.format(YYYYMMDD),
                    baseTime));  // 강수형태(0: 없음)
                items.add(new KmaItem("POP", fcstDate, hhmm, String.valueOf(10 + d), nx, ny,
                    baseDate.format(YYYYMMDD), baseTime)); // 강수확률
                items.add(new KmaItem("PCP", fcstDate, hhmm, "0", nx, ny, baseDate.format(YYYYMMDD),
                    baseTime));  // 강수량
                items.add(new KmaItem("SKY", fcstDate, hhmm, "3", nx, ny, baseDate.format(YYYYMMDD),
                    baseTime));  // 하늘상태(맑음~흐림 코드)
                items.add(
                    new KmaItem("WSD", fcstDate, hhmm, "2.5", nx, ny, baseDate.format(YYYYMMDD),
                        baseTime)); // 풍속(m/s)
            }

            // 일최저/최고 (보통 TMN: 0600, TMX: 1500에 제공되는 패턴)
            items.add(new KmaItem("TMN", fcstDate, "0600", String.valueOf(10 + d), nx, ny,
                baseDate.format(YYYYMMDD), baseTime));
            items.add(new KmaItem("TMX", fcstDate, "1500", String.valueOf(25 + d), nx, ny,
                baseDate.format(YYYYMMDD), baseTime));
        }
        return items;
    }
}
