package com.onepiece.otboo.domain.clothes.service.parser;

import com.onepiece.otboo.domain.clothes.dto.data.ParsedClothesInfoDto;
import java.io.IOException;
import java.util.List;

public interface ClothesInfoParser {

    static ClothesInfoParser choose(List<ClothesInfoParser> parsers, String url) {
        return parsers.stream()
            .filter(p -> p.supports(url))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 쇼핑몰 URL: " + url));
    }

    boolean supports(String url);

    ParsedClothesInfoDto parse(String url) throws IOException;
}
