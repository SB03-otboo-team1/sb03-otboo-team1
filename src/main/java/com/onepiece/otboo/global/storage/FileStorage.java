package com.onepiece.otboo.global.storage;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    String uploadFile(String prefix, MultipartFile image) throws IOException;

    void deleteFile(String key);
}
