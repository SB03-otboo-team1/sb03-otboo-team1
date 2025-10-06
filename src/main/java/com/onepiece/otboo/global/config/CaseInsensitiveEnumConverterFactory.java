package com.onepiece.otboo.global.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

public class CaseInsensitiveEnumConverterFactory implements ConverterFactory<String, Enum> {

    @Override
    public <T extends Enum> Converter<String, T> getConverter(@NonNull Class<T> targetType) {
        return new CaseInsensitiveEnumConverter<>(targetType);
    }

    private static final class CaseInsensitiveEnumConverter<T extends Enum> implements
        Converter<String, T> {

        private final Class<T> enumType;

        private CaseInsensitiveEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            if (!StringUtils.hasText(source)) {
                return null;
            }

            String s = source.trim();

            // camelCase → snake_case (createdAt → created_at)
            s = s.replaceAll("([a-z])([A-Z])", "$1_$2");

            // 하이픈/공백/점 등 비문자 구분자 → 언더스코어
            s = s.replaceAll("[\\s\\-.]+", "_");

            // 최종적으로 대문자화
            String key = s.toUpperCase();

            return (T) Enum.valueOf(this.enumType, key);
        }
    }
}
