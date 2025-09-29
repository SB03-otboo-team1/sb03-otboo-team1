package com.onepiece.otboo.global.util;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ArrayUtil {

    public static int indexOf(String[] arr, String v) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(v)) {
                return i;
            }
        }
        return -1;
    }

    public static String joinString(List<String> list) {
        if (list == null) return "";
        return list.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining(","));
    }
}
