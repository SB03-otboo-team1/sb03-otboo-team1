package com.onepiece.otboo.global.storage;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

  String uploadFile(MultipartFile file) throws IOException;
  InputStream getFile(String imageUrl);
}
