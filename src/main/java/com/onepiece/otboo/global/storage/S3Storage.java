package com.onepiece.otboo.global.storage;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Storage implements FileStorage{

  private final S3Client s3Client;

  @Value("${aws.s3.bucket}")
  private String bucket;

  @Value("${aws.s3.region}")
  private String region;

  private final ApplicationEventPublisher eventPublisher;

  @Override
  public String uploadFile(MultipartFile file) throws IOException {
    log.info("S3에 이미지 업로드 시작");

    String originalFilename = file.getOriginalFilename();
    String extension = StringUtils.getFilenameExtension(originalFilename);
    if (!StringUtils.hasText(extension)) {
        throw new IllegalArgumentException("파일 확장자를 확인할 수 없습니다.");
    }
    String key = UUID.randomUUID() + "." + extension;

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(file.getContentType())
        .contentLength(file.getSize())
        .build();

      S3Utilities utilities = s3Client.utilities();
      String imageUrl = utilities.getUrl(GetUrlRequest.builder()
          .bucket(bucket)
          .key(key)
          .build()).toExternalForm();

    try {
      s3Client.putObject(
          request,
          RequestBody.fromInputStream(file.getInputStream(), file.getSize())
      );

      log.info("S3에 이미지 업로드 성공 - 경로: {}", imageUrl);

      return imageUrl;

    } catch (S3Exception e) {
      log.error("S3 업로드 실패 - S3 서비스 오류 발생", e);

      throw e;
    } catch (IOException e) {
      log.error("S3 업로드 실패 - 파일 업로드 오류 발생", e);

      throw e;
    }
  }

  @Override
  public void deleteFile(String imageUrl) {
    log.info("delete file from s3 bucket");

    String key = URI.create(imageUrl).getPath().substring(1);

    s3Client.deleteObject(
        DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()
    );

    log.info("delete file from s3 bucket success: {}", key);
  }
}
