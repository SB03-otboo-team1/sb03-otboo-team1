package com.onepiece.otboo.infra.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LatLonToXYConverterTest {

    @Test
    void 위도_경도_데이터_격자_좌표로_변환_테스트() {

        // given
        // 경기도 시흥시 은행동 위도, 경도
        double latitude = 37.4335027777777;
        double longitude = 126.800077777777;

        // when
        LatLonToXYConverter.Point point = LatLonToXYConverter.latLonToXY(latitude, longitude);

        // then
        assertEquals(57, point.x);
        assertEquals(124, point.y);
    }
}