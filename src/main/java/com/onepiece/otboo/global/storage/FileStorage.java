package com.onepiece.otboo.global.storage;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

  String uploadFile(String prefix, MultipartFile file) throws IOException;

  void deleteFile(String imageUrl);
}
