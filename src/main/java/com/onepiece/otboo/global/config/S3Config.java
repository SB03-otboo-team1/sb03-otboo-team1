package com.onepiece.otboo.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 설정 클래스
 * S3 클라이언트를 구성합니다.
 */
@Configuration
public class S3Config {

    @Value("${aws.storage.access-key}")
    private String accessKey;

    @Value("${aws.storage.secret-key}")
    private String secretKey;

    @Value("${aws.storage.region}")
    private String region;

    /**
     * S3 클라이언트를 생성합니다.
     *
     * @return S3Client 인스턴스
     */
    @Bean
    public S3Client s3Client(AwsBasicCredentials awsBasicCredentials) {
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .build();
    }

    @Bean
    public AwsBasicCredentials awsBasicCredentials() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return awsBasicCredentials;
    }

    @Bean
    public S3Presigner s3Presigner(AwsBasicCredentials awsBasicCredentials) {
        return S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .build();
    }
}

