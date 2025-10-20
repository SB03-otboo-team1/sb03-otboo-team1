package com.onepiece.otboo.global.storage;

import com.onepiece.otboo.global.storage.exception.FileSizeExceededException;
import com.onepiece.otboo.global.storage.exception.InvalidFileTypeException;
import com.onepiece.otboo.global.storage.payload.UploadPayload;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Component
@ConditionalOnProperty(name = "aws.storage.type", havingValue = "s3")
public class S3Storage implements FileStorage {

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    private final String bucket;

    private final long MAX_SIZE = 5 * 1024 * 1024;

    @Value("${aws.storage.presigned-url-expiration}")
    private long presignedExpiration;

    public S3Storage(S3Client s3Client,
        S3Presigner s3Presigner,
        @Value("${aws.storage.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
    }

    @Override
    public String uploadFile(String prefix, MultipartFile image) throws IOException {

        // 파일 타입 검증
        String contentType = image.getContentType();
        validateImage(contentType, image.getSize());

        String key = buildKey(prefix, image.getOriginalFilename());

        uploadInternal(key, image.getBytes());

        return key;
    }

    @Override
    public String uploadBytes(String prefix, UploadPayload payload) throws IOException {
        String contentType = payload.contentType();
        validateImage(contentType, payload.size());

        String key = buildKey(prefix, payload.originalFilename());

        uploadInternal(key, payload.bytes());

        return key;
    }

    @Override
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            log.warn("[S3Storage] S3에서 이미지 삭제 중 오류 발생- key: {}", key, e);
        }
    }

    public String generatePresignedUrl(String key) {
        // Presigned Url 생성
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignedExpiration))
                .getObjectRequest(getRequest)
                .build();

            return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
        } catch (Exception e) {
            log.warn("[S3Storage] Presigned URL 생성 실패, S3Utilities URL로 폴백 시도 - key: {}", key, e);
            URL url = s3Client.utilities().getUrl(GetUrlRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
            return url.toString();
        }
    }

    private void validateImage(String contentType, long size) {
        if (contentType == null) {
            throw new InvalidFileTypeException(contentType);
        }
        if (size > MAX_SIZE) {
            throw new FileSizeExceededException(size, MAX_SIZE);
        }
    }

    private String buildKey(String prefix, String originalFilename) {
        String original = (originalFilename != null && !originalFilename.isBlank())
            ? originalFilename
            : "image";
        return prefix + UUID.randomUUID() + "." + original;
    }

    private PutObjectRequest buildPutRequest(String key) {
        return PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();
    }

    private void uploadInternal(String key, byte[] bytes) {
        PutObjectRequest putRequest = buildPutRequest(key);
        try {
            s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));
        } catch (AwsServiceException e) { // 서버측(Service) 예외
            log.error("S3 업로드 실패 - S3 서비스 오류 발생 key: {}", key, e);
            throw e;
        } catch (SdkClientException e) {   // 클라이언트측(SDK) 예외
            log.error("S3 업로드 실패 - SDK 클라이언트 오류 발생 key: {}", key, e);
            throw e;
        }
    }
}