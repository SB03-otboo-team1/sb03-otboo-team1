package com.onepiece.otboo.global.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum SortDirection {
    ASCENDING, DESCENDING;

    @JsonValue
    public String value() { return name(); }

    @JsonCreator
    public static SortDirection from(String v) {
        if (v == null) return null;
        String s = v.trim().toUpperCase(Locale.ROOT);
        return switch (s) {
            case "ASC", "ASCENDING" -> ASCENDING;
            case "DESC", "DESCENDING" -> DESCENDING;
            default -> throw new IllegalArgumentException("Unsupported sortDirection: " + v);
        };
    }
}