package com.onepiece.otboo.domain.clothes.service.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.clothes.dto.data.ParsedClothesInfoDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MusinsaParser implements ClothesInfoParser {

    private final ObjectMapper objectMapper;
    private final ClothesAttributeDefRepository defRepository;

    @Override
    public boolean supports(String url) {
        return url.contains("musinsa");
    }

    @Override
    public ParsedClothesInfoDto parse(String url) {

        List<String> attrList = defRepository.findAll().stream().map(ClothesAttributeDefs::getName)
            .toList();

        try {
            // HTML 요청
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(8000)
                .get();

            Element script = doc.select("script:containsData(window.__MSS__.product.state)")
                .first();
            if (script == null) {
                throw new IllegalStateException("window.__MSS__.product.state 스크립트를 찾을 수 없습니다.");
            }

            String scriptText = script.html();
            String json = extractJson(scriptText);

            JsonNode root = objectMapper.readTree(json);

            String name = root.path("goodsNm").asText();

            String imageUrl = extractMainImage(root.path("thumbnailImageUrl"));
            if (imageUrl == null) {
                imageUrl = parseImageFromMeta(doc);
            }

            ClothesType type = extractType(root);

            Map<String, String> attributes = extractAttributes(attrList, root);

            return new ParsedClothesInfoDto(name, type, imageUrl, attributes);

        } catch (IOException e) {
            throw new RuntimeException("무신사 상품 파싱 중 오류 발생", e);
        }
    }

    private String extractJson(String scriptText) {
        Matcher matcher = Pattern.compile("window\\.__MSS__\\.product\\.state\\s*=\\s*(\\{.*?\\});",
            Pattern.DOTALL).matcher(scriptText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("window.__MSS__.product.state JSON 추출 실패");
    }

    private String extractMainImage(JsonNode images) {
        String path = images.asText();
        if (path.isEmpty()) {
            return null;
        }
        return "https://image.msscdn.net" + path;
    }

    private ClothesType extractType(JsonNode root) {
        String type = null;
        try {
            JsonNode category = root.path("category");
            type = category.path("categoryDepth1Title").asText();
            if (type.equals("스포츠/레저")) {
                type = category.path("categoryDepth2Title").asText();
            }
            if (type.equals("원피스/스커트")) {
                return category.path("categoryDepth2Title").asText().contains("스커트")
                    ? ClothesType.BOTTOM : ClothesType.DRESS;
            } else if (type.equals("상의")) {
                return ClothesType.TOP;
            } else if (type.equals("바지")) {
                return ClothesType.BOTTOM;
            } else if (type.equals("신발")) {
                return ClothesType.SHOES;
            } else if (type.equals("아우터")) {
                return ClothesType.OUTER;
            } else if (type.equals("가방")) {
                return ClothesType.BAG;
            } else if (type.equals("패션소품")) {
                type = category.path("categoryDepth2Title").asText();
                if (type.equals("주얼리") || type.equals("액세서리") || type.equals("시계") || type.equals(
                    "벨트") || type.equals("선글라스/안경테")) {
                    return ClothesType.ACCESSORY;
                } else if (type.equals("모자")) {
                    return ClothesType.HAT;
                } else if (type.equals("양말/레그웨어")) {
                    return ClothesType.SOCKS;
                } else if (type.equals("머플러")) {
                    return ClothesType.SCARF;
                }
            } else if (type.equals("속옷/홈웨어")) {
                return ClothesType.UNDERWEAR;
            } else {
                return ClothesType.ETC;
            }

        } catch (Exception e) {
            return ClothesType.ETC;
        }

        return ClothesType.ETC;
    }

    private Map<String, String> extractAttributes(List<String> attrList, JsonNode root) {
        Map<String, String> attrs = new LinkedHashMap<>();

        if (attrList.isEmpty()) {
            return attrs;
        } else {
            for (String attr : attrList) {
                if (attr.equals("계절")) {
                    String seasonCode = root.path("season").asText();
                    attrs.put("계절", parseSeason(seasonCode));
                } else if (attr.equals("색깔")) {
                    String name = root.path("goodsNm").asText();
                    attrs.put("색깔", parseColor(name));
                } else {
                    String value = root.path(attr).asText();
                    attrs.put(attr, value);
                }
            }
        }
        return attrs;
    }

    private String parseSeason(String seasonCode) {
        return switch (seasonCode) {
            case "1" -> "봄";
            case "2" -> "여름";
            case "3" -> "가을";
            case "4" -> "겨울";
            default -> null;
        };
    }

    private String parseColor(String name) {

        if (name.contains("블랙") || name.contains("BLACK") || name.contains("화이트") || name.contains(
            "WHITE") || name.contains("그레이") || name.contains("GRAY") || name.contains("GREY")
            || name.contains("차콜") || name.contains("CHARCOAL") || name.contains("네이비")
            || name.contains("NAVY")) {
            return "무채색";
        } else if (name.contains("브라운") || name.contains("BROWN") || name.contains("아이보리")
            || name.contains("IVORY") || name.contains("베이지")
            || name.contains("BEIGE") || name.contains("크림") || name.contains("CREAM")
            || name.contains("올리브") || name.contains("OLIVE")) {
            return "갈색계열";
        } else {
            return "비비드";
        }
    }

    private String parseImageFromMeta(Document doc) {
        String imageUrl = doc.select("meta[property=og:image]").attr("content");
        if (imageUrl.isEmpty()) {
            imageUrl = doc.select("meta[name=twitter:image]").attr("content");
        }

        return imageUrl;
    }
}
