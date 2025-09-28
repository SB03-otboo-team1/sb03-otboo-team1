package com.onepiece.otboo.domain.clothes.fixture;

import com.onepiece.otboo.global.storage.FileStorage;
import com.onepiece.otboo.global.storage.S3Storage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * S3 테스트용 픽스처 클래스
 * S3Storage의 Mock 객체와 테스트 데이터를 제공합니다.
 */
public class S3Fixture {

  /**
   * 테스트용 MockMultipartFile을 생성합니다.
   *
   * @param filename 파일명
   * @param contentType 콘텐츠 타입
   * @param content 파일 내용
   * @return MockMultipartFile 인스턴스
   */
  public static MockMultipartFile createMockImageFile(String filename, String contentType, String content) {
    return new MockMultipartFile(
        "file",
        filename,
        contentType,
        content.getBytes(StandardCharsets.UTF_8)
    );
  }

  /**
   * 기본 이미지 파일을 생성합니다.
   *
   * @return 기본 테스트 이미지 파일
   */
  public static MockMultipartFile createDefaultImageFile() {
    return createMockImageFile(
        "test-image.jpg",
        "image/jpeg",
        "fake-image-content"
    );
  }

  /**
   * 테스트용 S3Storage Mock을 생성합니다.
   *
   * @param s3Client Mock S3Client
   * @param eventPublisher Mock ApplicationEventPublisher
   * @param bucket S3 버킷명
   * @param region S3 리전
   * @return S3Storage Mock 인스턴스
   */
  public static S3Storage createMockS3Storage(
      S3Client s3Client,
      ApplicationEventPublisher eventPublisher,
      String bucket,
      String region) {

    return new S3Storage(s3Client, eventPublisher) {
      @Override
      public String uploadFile(MultipartFile file) throws IOException {
        // 테스트용 가짜 URL 반환
        String mockKey = "test-key-" + System.currentTimeMillis();
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, mockKey);
      }

      @Override
      public InputStream getFile(String imageUrl) {
        // 테스트용 가짜 InputStream 반환
        return new ByteArrayInputStream("fake-image-data".getBytes(StandardCharsets.UTF_8));
      }

      @Override
      public void deleteFile(String imageUrl) {
        // 테스트에서는 실제 삭제하지 않음
      }
    };
  }

  /**
   * 테스트용 기본 설정으로 S3Storage Mock을 생성합니다.
   *
   * @param s3Client Mock S3Client
   * @param eventPublisher Mock ApplicationEventPublisher
   * @return S3Storage Mock 인스턴스
   */
  public static S3Storage createDefaultMockS3Storage(
      S3Client s3Client,
      ApplicationEventPublisher eventPublisher) {
    return createMockS3Storage(s3Client, eventPublisher, "test-bucket", "ap-northeast-2");
  }

  /**
   * 테스트용 이미지 URL을 생성합니다.
   *
   * @param key S3 객체 키
   * @return 테스트용 이미지 URL
   */
  public static String createTestImageUrl(String key) {
    return String.format("https://test-bucket.s3.ap-northeast-2.amazonaws.com/%s", key);
  }

  /**
   * 랜덤한 테스트용 이미지 URL을 생성합니다.
   *
   * @return 랜덤 테스트용 이미지 URL
   */
  public static String createRandomTestImageUrl() {
    return createTestImageUrl("test-key-" + System.currentTimeMillis());
  }

  /**
   * 테스트용 FileStorage 인터페이스 구현체를 생성합니다.
   *
   * @return FileStorage Mock 구현체
   */
  public static FileStorage createMockFileStorage() {
    return new FileStorage() {
      @Override
      public String uploadFile(MultipartFile file) throws IOException {
        return createRandomTestImageUrl();
      }

      @Override
      public InputStream getFile(String imageUrl) {
        return new ByteArrayInputStream("fake-image-data".getBytes(StandardCharsets.UTF_8));
      }

      @Override
      public void deleteFile(String imageUrl) {
        // 테스트에서는 실제 삭제하지 않음
      }
    };
  }

  /**
   * 테스트용 대용량 이미지 파일을 생성합니다.
   *
   * @param sizeInMB 파일 크기 (MB)
   * @return 대용량 테스트 파일
   */
  public static MockMultipartFile createLargeImageFile(int sizeInMB) {
    byte[] content = new byte[sizeInMB * 1024 * 1024];
    return new MockMultipartFile(
        "file",
        "large-image.jpg",
        "image/jpeg",
        content
    );
  }

  /**
   * 테스트용 잘못된 형식의 파일을 생성합니다.
   *
   * @return 잘못된 형식의 테스트 파일
   */
  public static MockMultipartFile createInvalidFile() {
    return new MockMultipartFile(
        "file",
        "test.txt",
        "text/plain",
        "invalid-content".getBytes(StandardCharsets.UTF_8)
    );
  }
}