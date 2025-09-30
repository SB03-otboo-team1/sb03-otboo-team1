package com.onepiece.otboo.global.storage.payload;

public record UploadPayload(
    byte[] bytes,
    String originalFilename,
    String contentType,
    long size
) {

}