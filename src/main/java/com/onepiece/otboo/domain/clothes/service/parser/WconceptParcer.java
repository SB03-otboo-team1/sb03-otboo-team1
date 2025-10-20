package com.onepiece.otboo.domain.clothes.service.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.clothes.dto.data.ParsedClothesInfoDto;
import com.onepiece.otboo.domain.clothes.entity.ClothesAttributeDefs;
import com.onepiece.otboo.domain.clothes.entity.ClothesType;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import java.io.IOException;
import java.util.LinkedHashMap;
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
public class WconceptParcer implements ClothesInfoParser {

    private final ObjectMapper objectMapper;
    private final ClothesAttributeDefRepository defRepository;

    @Override
    public boolean supports(String url) {
        return url.contains("wconcept.co.kr");
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

            String name = doc.select("input[id=hidItemName]").attr("value");

            String imageUrl = extractMainImage(doc);

            ClothesType type = extractType(doc);

            Map<String, String> attributes = extractAttributes(attrList, doc);

            return new ParsedClothesInfoDto(name, type, imageUrl, attributes);

        } catch (IOException e) {
            throw new RuntimeException("무신사 상품 파싱 중 오류 발생", e);
        }
    }

    private String extractMainImage(Document doc) {
        String image = doc.select("input[name=hidImageUrl]").attr("value");
        if (!image.isBlank()) {
            return image.startsWith("http") ? image : "https:" + image;
        }

        Element firstImg = doc.select("div.img_area img#img_01").first();
        if (firstImg != null) {
            String src = firstImg.attr("src");
            return src.startsWith("http") ? src : "https:" + src;
        }

        return null;
    }

    private ClothesType extractType(Document doc) {
        String typeRaw = doc.select("input[id=hidAddCatFix]").attr("value");
        System.out.println(
            "[W컨셉 파서] doc.select(\"input[id=hidAddCatFix]\").attr(\"value\")" + typeRaw);
        String type1 = typeRaw.split("\\^")[2];
        String type2 = typeRaw.split("\\^")[3];
        String type3 = typeRaw.split("\\^")[4];

        if (type1.equals("의류")) {
            if (type2.contains("원피스")) {
                return ClothesType.DRESS;
            } else if (type2.contains("상의")) {
                return ClothesType.TOP;
            } else if (type2.contains("바지")) {
                return ClothesType.BOTTOM;
            } else if (type2.contains("아우터")) {
                return ClothesType.OUTER;
            } else if (type2.equals("언더웨어")) {
                return ClothesType.UNDERWEAR;
            }
        } else if (type1.equals("가방")) {
            return ClothesType.BAG;
        } else if (type1.contains("신발")) {
            return ClothesType.SHOES;
        } else if (type1.equals("액세서리")) {
            if (type2.equals("주얼리") || type2.contains("액세서리") || type2.equals("시계")
                || type2.equals("14k골드") || type2.equals("스카프/안경테")) {
                return ClothesType.ACCESSORY;
            } else if (type2.equals("모자")) {
                return ClothesType.HAT;
            } else if (type2.equals("스카프/머플러")) {
                return ClothesType.SCARF;
            }
        } else if (type3.equals("양말")) {
            return ClothesType.SOCKS;
        } else if (type2.equals("언더웨어")) {
            return ClothesType.UNDERWEAR;
        } else {
            return ClothesType.ETC;
        }

        return ClothesType.ETC;
    }

    private Map<String, String> extractAttributes(List<String> attrList, Document doc) {
        Map<String, String> attrs = new LinkedHashMap<>();

        if (attrList.isEmpty()) {
            return attrs;
        } else {
            for (String attr : attrList) {
                if (attr.equals("색깔")) {
                    String name = doc.select("input#hidItemName").attr("value");
                    attrs.put("색깔", parseColor(name));
                } else {
                    String value = doc.select("input#" + attr).attr("value");
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
