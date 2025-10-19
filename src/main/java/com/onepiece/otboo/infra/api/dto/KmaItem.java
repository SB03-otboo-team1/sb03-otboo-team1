package com.onepiece.otboo.infra.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KmaItem(
    String category, // TMP, PTY, SKY, POP, PCP, REH, WSD, TMX, TMN ...
    String fcstDate, // yyyyMMdd
    String fcstTime, // HHmm
    String fcstValue,
    int nx,
    int ny,
    String baseDate,
    String baseTime
) {
    public KmaItem withBase(String baseDate, String baseTime) {
        return new KmaItem(category, fcstDate, fcstTime, fcstValue, nx, ny, baseDate, baseTime);
    }
}
