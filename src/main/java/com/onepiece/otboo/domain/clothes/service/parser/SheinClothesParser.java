package com.onepiece.otboo.domain.clothes.service.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepiece.otboo.domain.clothes.dto.data.ParsedClothesInfoDto;
import com.onepiece.otboo.domain.clothes.repository.ClothesAttributeDefRepository;
import java.io.IOException;
import java.util.Iterator;
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
public class SheinClothesParser implements ClothesInfoParser {

    private final ObjectMapper objectMapper;
    private final ClothesAttributeDefRepository defRepository;

    @Override
    public boolean supports(String url) {
        return url.contains("shein");
    }

    @Override
    public ParsedClothesInfoDto parse(String url) {

        try {
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                    + "AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/140.0.0.0 Safari/537.36")
                .timeout(5000)
                .get();

            Element script = doc.select("script").stream()
                .filter(e -> e.html().contains("window.gbRawData"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("window.gbRawData를 찾을 수 없습니다."));

            String scriptHtml = script.html();
            String json = extractJson(scriptHtml);
            int start = scriptHtml.indexOf("window.gbRawData = {") + "window.gbRawData =".length();
            int end = scriptHtml.indexOf("};");
            String jsonRaw = scriptHtml.substring(start, end + 1).trim();

            // JSON 파싱
            JsonNode root = objectMapper.readTree(jsonRaw);
            JsonNode productInfo = root.get("/modules/productInfo");
            JsonNode details = productInfo.path("productDescriptionInfo").path("productDetails");

            // 상품 이름
            String clothesName = extractProductName(root);

            // 상품 타입 - 상의, 하의, 원피스, 아우터, 속옷, 악세서리, 신발, 양말, 모자, 가방, 스카프, 기타
            String clothesType = extractProductType(productInfo);

            // 상품 이미지
            String imageUrl = extractProductImage(productInfo);

            // 의상 속성
            List<String> defs = getDefs();

            // 상품 속성 - 계절, 색상, 스타일 등
            Map<String, String> attributes = extractAttributes(defs, details);

            // ParsedClothesInfoDto 생성
            return ParsedClothesInfoDto.builder()
                .clothesName(clothesName)
                .clothesType(clothesType)
                .imageUrl(imageUrl)
                .attributes(attributes)
                .build();

        } catch (IOException e) {
            throw new RuntimeException("SHEIN 파싱 중 오류 발생", e);
        }
    }

    private List<String> getDefs() {
        List<String> defs = defRepository.findAll().stream().map(
            d -> d.getName()
        ).toList();

        return defs;
    }

    private String extractJson(String scriptHtml) {
        Matcher matcher = Pattern.compile("window\\.gbRawData\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL)
            .matcher(scriptHtml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("window.gbRawData JSON 추출 실패");
    }

    private String extractProductName(JsonNode root) {
        try {
            String innerHtml = root.path("googleSEO").get(1).path("innerHtml").asText();
            JsonNode parsed = objectMapper.readTree(innerHtml);
            return parsed.get(0).path("name").asText();
        } catch (Exception e) {
            log.warn("상품 이름 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    private String extractProductType(JsonNode productInfo) {
        String type = null;
        try {
            JsonNode cateInfos = productInfo.path("cateInfos");
            Iterator<String> fieldNames = cateInfos.fieldNames();
            String deepest = null;
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                JsonNode cate = cateInfos.get(key);
                if (cate.path("is_leaf").asText().equals("1")) {
                    deepest = cate.path("category_name").asText();
                }
            }
            type = deepest != null ? deepest : "기타";
        } catch (Exception e) {
            type = "기타";
        }

        if (type.contains("드레스") || type.contains("투피스") || type.contains("점프수트") || type.contains(
            "바디수트") || type.contains("유니타드") || type.contains("코오드") || type.contains("코디네이트")
            || type.contains("수트")) {
            return "원피스";
        } else if (type.contains("셔츠") || type.contains("블라우스") || type.contains("상의")
            || type.contains("스웨터") || type.contains("탱크탑") || type.contains("후드티")) {
            return "상의";
        } else if (type.contains("바지") || type.contains("스커트") || type.contains("팬츠")
            || type.contains("레깅스")) {
            return "하의";
        } else if (type.contains("재킷") || type.contains("코트") || type.contains("블레이저")
            || type.contains("가디건") || type.contains("조끼") || type.contains("샤켓")) {
            return "아우터";
        } else if (type.contains("브라") || type.contains("브래지어") || type.contains("팬티")
            || type.contains("트렁크") || type.contains("브리프") || type.contains("언더셔츠")
            || type.contains("속옷")) {
            return "속옷";
        } else if (type.contains("양말") || type.contains("삭스") || type.contains("타이츠")
            || type.contains("스타킹") || type.contains("레그워머")) {
            return "양말";
        } else if (type.contains("신발") || type.contains("웨지") || type.contains("펌프")
            || type.contains("부츠") || type.contains("샌들") || type.contains("슬리퍼") || type.contains(
            "슈즈") || type.contains("운동화") || type.contains("스니커즈") || type.contains("정장화")) {
            return "신발";
        } else if (type.contains("모자") || type.contains("햇") || type.contains("베레모")
            || type.contains("발라클라바") || type.contains("캡")) {
            return "모자";
        } else {
            return "기타";
        }
    }

    private String extractProductImage(JsonNode productInfo) {
        try {
            JsonNode images = productInfo.path("currentSkcImgInfo").path("skcImages");
            if (images.isArray() && images.size() > 0) {
                String img = images.get(0).asText();
                return img.startsWith("//") ? "https:" + img : img;
            }
            return null;
        } catch (Exception e) {
            log.warn("대표 이미지 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, String> extractAttributes(List<String> defs, JsonNode details) {
        Map<String, String> map = new LinkedHashMap<>();
        if (details.isMissingNode() || !details.isArray()) {
            return map;
        }

        for (JsonNode attr : details) {
            String attrName = attr.path("attr_name").asText();
            String attrValue = attr.path("attr_value").asText();

            if (!defs.contains(attrName)) {
                continue;
            }

            switch (attrName) {
                case "색" -> {
                    if (attrValue.contains("블랙") || attrValue.contains("화이트") || attrValue.contains(
                        "그레이") || attrValue.contains("차콜") || attrValue.contains("네이비")) {
                        map.put("색깔", "무채색");
                    } else if (attrValue.contains("브라운") || attrValue.contains("아이보리")
                        || attrValue.contains("베이지")) {
                        map.put("색깔", "갈색계열");
                    } else {
                        map.put("색깔", "비비드");
                    }
                }
                case "스타일" -> {
                    if (attrValue.contains("엘레강스") || attrValue.contains("글래머러스")) {
                        map.put("스타일", "포멀");
                    } else if (attrValue.contains("캐주얼")) {
                        map.put("스타일", "스트릿");
                    } else {
                        map.put("스타일", "캐주얼");
                    }
                }
                case "계절" -> {
                    if (attrValue.contains("봄")) {
                        map.put("계절", "봄");
                    } else if (attrValue.contains("가을")) {
                        map.put("계절", "가을");
                    } else if (attrValue.contains("여름")) {
                        map.put("계절", "여름");
                    } else if (attrValue.contains("겨울")) {
                        map.put("계절", "겨울");
                    } else {
                        map.put("계절", "가을");
                    }
                }
            }
        }
        return map;
    }

}
