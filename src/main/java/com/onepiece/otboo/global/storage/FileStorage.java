package com.onepiece.otboo.global.storage;

import com.onepiece.otboo.global.storage.payload.UploadPayload;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    String uploadFile(String prefix, MultipartFile image) throws IOException;

    String uploadBytes(String prefix, UploadPayload payload) throws IOException;

    void deleteFile(String key);
}