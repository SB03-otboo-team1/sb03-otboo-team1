package com.onepiece.otboo.domain.clothes.service.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.clothes.dto.data.ParsedClothesInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MusinsaParser implements ClothesInfoParser {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String url) {
        return url.contains("musinsa");
    }

    @Override
    public ParsedClothesInfoDto parse(String url) {

        return null;
    }
}
