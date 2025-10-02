package com.onepiece.otboo.domain.clothes.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ClothesAttributeDefNotFoundException extends ClothesException{

    public ClothesAttributeDefNotFoundException(String message) {
        super(ErrorCode.CLOTHES_ATTRIBUTE_DEF_NOT_FOUND, Map.of("message", message));
    }
    private ClothesAttributeDefNotFoundException(Map<String, Object> details) {
        super(ErrorCode.CLOTHES_ATTRIBUTE_DEF_NOT_FOUND, details);
    }

    public static ClothesAttributeDefNotFoundException byId(UUID definitionId) {
        return new ClothesAttributeDefNotFoundException(Map.of("definitionId", definitionId));
    }

}
