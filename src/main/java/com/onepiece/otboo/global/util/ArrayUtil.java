package com.onepiece.otboo.global.util;

public class ArrayUtil {

    public static int indexOf(String[] arr, String v) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(v)) {
                return i;
            }
        }
        return -1;
    }
}
