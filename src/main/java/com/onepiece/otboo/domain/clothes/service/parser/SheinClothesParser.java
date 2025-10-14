package com.onepiece.otboo.domain.clothes.service.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.clothes.dto.data.ParsedClothesInfoDto;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SheinClothesParser implements ClothesInfoParser {

    private final ObjectMapper objectMapper;
    private final ClothesAttributeDefRepository defRepository;

    @Override
    public boolean supports(String url) {
        return url.contains("shein");
    }

    @Override
    public ParsedClothesInfoDto parse(String url) throws IOException {
        Document doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/140.0.0.0 Safari/537.36")
            .timeout(5000)
            .get();

        Element script = doc.select("script").stream()
            .filter(e -> e.html().contains("window.gbRawData"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("gbRawData not found"));

        String scriptHtml = script.html();
        int start = scriptHtml.indexOf("window.gbRawData = {") + "window.gbRawData =".length();
        int end = scriptHtml.indexOf("};");
        String jsonRaw = scriptHtml.substring(start, end + 1).trim();

        JsonNode root = objectMapper.readTree(jsonRaw);

        String imageUrl = null;
        JsonNode imageNode = root.get("/modules/productInfo/currentSkcImgInfo/skcImages");
        if (imageNode != null && imageNode.isArray() && imageNode.size() > 0) {
            imageUrl = imageNode.get(0).asText().replace("\\", "");
        }

        String name = root.get("/modules/productInfo/productInfo/productName").asText();
        String type = root.get("/modules/productInfo/productInfo/productType").asText();

        // 의상 속성
        List<String> defs = getDefs();

        Map<String, String> attrValues = new HashMap<>();

        JsonNode attrs = root.get("/modules/productInfo/productInfo/productAttrs");
        if (attrs != null && attrs.isArray() && attrs.size() > 0) {
            for (JsonNode attr : attrs) {
                String attrName = attr.path("attrName").asText();
                String attrValue = attr.path("attrValue").asText();

                for (String def : defs) {
                    if (def.equals(attrName) && attrValue.length() > 0) {
                        attrValues.put(attrName, attrValue);
                    }
                }
            }
        }

        ParsedClothesInfoDto dto =
            ParsedClothesInfoDto.builder()
                .name(name)
                .type(type)
                .imageUrl(imageUrl)
                .attributes(attrValues)
                .build();

        return dto;

    }

    private List<String> getDefs() {
        List<String> defs = defRepository.findAll().stream().map(
            d -> d.getName()
        ).toList();

        return defs;
    }

}
