package com.onepiece.otboo.infra.converter;

/**
 * 기상청 단기예보 OpenAPI에서 사용하는 Lambert Conformal Conic (LCC) 투영법을 기반으로 위도(Latitude), 경도(Longitude)를 격자
 * 좌표(X, Y)로 변환하는 유틸리티 클래스입니다.
 *
 * <p>이 변환은 기상청 단기예보 서비스에서 제공하는 격자 체계(NX=149, NY=253)를
 * 기준으로 하며, 남한 지역만을 대상으로 합니다.</p>
 *
 * <p>참고:
 * <a href="https://www.data.go.kr/">기상청 단기예보 오픈API 문서</a></p>
 *
 * <pre>
 * 사용 예시:
 * double lon = 126.929810;
 * double lat = 37.488201;
 * LatLonToXYConverter.Point point = LatLonToXYConverter.latLonToXY(lat, lon);
 * System.out.println(point); // 출력: X=59, Y=125
 * </pre>
 *
 *
 */
public class LatLonToXYConverter {

    // Lambert Conformal Conic (LCC) 투영법 파라미터
    private static final double RE = 6371.00877; // 지구 반경 (km)
    private static final double GRID = 5.0;      // 격자 간격 (km)
    private static final double SLAT1 = 30.0;    // 표준 위도 1 (degree)
    private static final double SLAT2 = 60.0;    // 표준 위도 2 (degree)
    private static final double OLON = 126.0;    // 기준 경도 (degree)
    private static final double OLAT = 38.0;     // 기준 위도 (degree)
    private static final double XO = 210 / GRID; // 기준점 X좌표
    private static final double YO = 675 / GRID; // 기준점 Y좌표

    private static final double DEGRAD = Math.PI / 180.0;

    private static final double sn;
    private static final double sf;
    private static final double ro;

    static {
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olat = OLAT * DEGRAD;

        double snTemp = Math.tan(Math.PI * 0.25 + slat2 * 0.5) /
            Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        snTemp = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(snTemp);
        sn = snTemp;

        double sfTemp = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sfTemp = Math.pow(sfTemp, sn) * Math.cos(slat1) / sn;
        sf = sfTemp;

        double roTemp = Math.tan(Math.PI * 0.25 + olat * 0.5);
        roTemp = (RE / GRID) * sf / Math.pow(roTemp, sn);
        ro = roTemp;
    }

    /**
     * 주어진 위도와 경도를 Lambert Conformal Conic 투영법을 사용하여
     * 기상청 격자 좌표(X, Y)로 변환합니다.
     *
     * @param lat 위도 값 (단위: degree, -90 ~ 90 권장)
     * @param lon 경도 값 (단위: degree, -180 ~ 180 권장)
     * @return 변환된 {@link Point} 객체 (격자 좌표 X, Y 포함)
     */
    public static Point latLonToXY(double lat, double lon) {
        double ra = Math.tan(Math.PI * 0.25 + (lat) * DEGRAD * 0.5);
        ra = (RE / GRID) * sf / Math.pow(ra, sn);

        double theta = lon * DEGRAD - OLON * DEGRAD;
        if (theta > Math.PI) {
            theta -= 2.0 * Math.PI;
        }
        if (theta < -Math.PI) {
            theta += 2.0 * Math.PI;
        }
        theta *= sn;

        int x = (int) Math.floor(ra * Math.sin(theta) + XO + 1.5);
        int y = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 1.5);

        return new Point(x, y);
    }

    public static class Point {

        public final int x;
        public final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}