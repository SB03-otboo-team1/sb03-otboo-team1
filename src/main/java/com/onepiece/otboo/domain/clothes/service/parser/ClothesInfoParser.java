package com.onepiece.otboo.domain.clothes.service.parser;

import com.onepiece.otboo.domain.clothes.dto.data.ParsedClothesInfoDto;
import java.io.IOException;

public interface ClothesInfoParser {

    boolean supports(String url);

    ParsedClothesInfoDto parse(String url) throws IOException;
}
