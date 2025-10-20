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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ZigzagParser implements ClothesInfoParser {

    private final ObjectMapper objectMapper;
    private final ClothesAttributeDefRepository defRepository;

    @Override
    public boolean supports(String url) {
        return url.contains("zigzag");
    }

    @Override
    public ParsedClothesInfoDto parse(String url) {

        List<String> attrList = defRepository.findAll().stream().map(ClothesAttributeDefs::getName)
            .toList();

        try {
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(8000)
                .get();

            Element nextData = doc.selectFirst("script#__NEXT_DATA__");
            if (nextData == null) {
                throw new IllegalStateException("__NEXT_DATA__ 스크립트를 찾을 수 없습니다.");
            }

            String json = nextData.html();
            JsonNode root = objectMapper.readTree(json);

            JsonNode productNode = root
                .path("props")
                .path("pageProps")
                .path("product");

            if (productNode.isMissingNode()) {
                throw new IllegalStateException("product 정보가 존재하지 않습니다.");
            }

            String name = productNode.path("name").asText();

            String imageUrl = extractMainImage(productNode.path("product_image_list"));

            ClothesType type = extractType(productNode.path("managed_category_list"));

            Map<String, String> attributes = extractAttributes(attrList, productNode);

            return new ParsedClothesInfoDto(name, type, imageUrl, attributes);

        } catch (IOException e) {
            throw new RuntimeException("지그재그 상품 파싱 중 오류 발생", e);
        }
    }

    private String extractMainImage(JsonNode imageList) {
        if (!imageList.isArray() || imageList.isEmpty()) {
            return null;
        }
        for (JsonNode img : imageList) {
            if ("MAIN".equalsIgnoreCase(img.path("image_type").asText())) {
                return img.path("url").asText();
            }
        }
        return imageList.get(0).path("url").asText();
    }

    private ClothesType extractType(JsonNode categories) {
        if (!categories.isArray() || categories.isEmpty()) {
            return ClothesType.ETC;
        }
        String type1 = categories.get(1).path("value").asText();
        String type2 = categories.get(2).path("value").asText();

        if (type1.equals("패션의류")) {
            String type3 = categories.get(3).path("value").asText();
            if (type3.contains("원피스") || type3.contains("점프수트") || type3.contains("투피스")) {
                return ClothesType.DRESS;
            } else if (type3.contains("니트") || type3.contains("셔츠") || type3.contains("블라우스")
                || type3.contains("맨투맨") || type3.contains("후드") || type3.contains("슬리브리스")) {
                return ClothesType.TOP;
            } else if (type3.contains("팬츠") || type3.contains("스커트")) {
                return ClothesType.BOTTOM;
            } else if (type3.contains("아우터") || type3.contains("카디건")) {
                return ClothesType.OUTER;
            } else if (type3.equals("언더웨어")) {
                return ClothesType.UNDERWEAR;
            } else if (type3.contains("요가/피트니스") || type3.contains("스포츠") || type3.contains(
                "레깅스")) {
                String type4 = categories.get(4).path("value").asText();
                if (type4.contains("팬츠") || (type4.contains("레깅스"))) {
                    return ClothesType.BOTTOM;
                } else if (type4.contains("셔츠")) {
                    return ClothesType.TOP;
                } else if (type4.contains("브라")) {
                    return ClothesType.UNDERWEAR;
                } else {
                    return ClothesType.ETC;
                }
            }
        } else if (type1.equals("가방")) {
            return ClothesType.BAG;
        } else if (type1.contains("신발")) {
            return ClothesType.SHOES;
        } else if (type1.equals("모자")) {
            return ClothesType.HAT;
        } else if (type1.equals("패션잡화")) {
            if (type2.equals("주얼리") || type2.contains("액세서리") || type2.equals("시계")
                || type2.equals("벨트") || type2.equals("아이웨어") || type2.equals("장갑")) {
                return ClothesType.ACCESSORY;

            } else if (type2.equals("스카프/머플러")) {
                return ClothesType.SCARF;
            } else if (type2.equals("양말") || type2.equals("스타킹")) {
                return ClothesType.SOCKS;
            } else {
                return ClothesType.ETC;
            }
        } else {
            return ClothesType.ETC;
        }

        return ClothesType.ETC;
    }

    private Map<String, String> extractAttributes(List<String> attrList, JsonNode productNode) {
        Map<String, String> attrs = new LinkedHashMap<>();

        if (attrList.isEmpty()) {
            return attrs;
        } else {
            for (String attr : attrList) {
                if (attr.equals("계절")) {
                    String name = productNode.path("name").asText();
                    attrs.put("계절", parseSeason(name));
                } else if (attr.equals("색깔")) {
                    String name = productNode.path("name").asText();
                    attrs.put("색깔", parseColor(name));
                } else {
                    String value = productNode.path(attr).asText();
                    attrs.put(attr, value);
                }
            }
        }

        return attrs;
    }

    private String parseSeason(String name) {

        if (name.contains("봄")) {
            return "봄";
        } else if (name.contains("여름")) {
            return "여름";
        } else if (name.contains("가을")) {
            return "가을";
        } else if (name.contains("겨울")) {
            return "겨울";
        } else {
            return null;
        }
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

}
