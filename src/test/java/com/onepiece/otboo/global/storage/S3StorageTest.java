package com.onepiece.otboo.global.storage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.onepiece.otboo.global.storage.exception.FileSizeExceededException;
import com.onepiece.otboo.global.storage.exception.InvalidFileTypeException;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3StorageTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private S3Storage s3Storage;

    private final String KEY = "image/";

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        s3Presigner = mock(S3Presigner.class);
        s3Storage = new S3Storage(s3Client, s3Presigner, "test-bucket");
    }

    @Test
    void 이미지를_업로드_하면_키를_반환해야_한다() throws IOException {

        // given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test-image-content".getBytes()
        );

        // when
        String key = s3Storage.uploadFile(KEY, file);

        // then
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(
            PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.bucket()).isEqualTo("test-bucket");
        assertThat(capturedRequest.key()).startsWith("image/");
        assertThat(key).isEqualTo(capturedRequest.key());
    }

    @Test
    void 파일_타입이_image가_아닌_경우_업로드에_실패한다() {

        // given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "text-content".getBytes()
        );

        // when
        Throwable thrown = catchThrowable(() -> s3Storage.uploadFile(KEY, file));

        // then
        assertThat(thrown)
            .isInstanceOf(InvalidFileTypeException.class);
    }

    @Test
    void 파일_크기가_5MB_보다_큰_경우_업로드에_실패한다() {

        // given
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "large.jpg",
            "image/jpeg",
            largeContent
        );

        Throwable thrown = catchThrowable(() -> s3Storage.uploadFile(KEY, file));

        // then
        assertThat(thrown)
            .isInstanceOf(FileSizeExceededException.class);
    }

    @Test
    void presigned_url_생성_테스트() {

        // given
        String key = "image/test.jpg";
        URL fakeUrl = mock(URL.class);
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);

        given(fakeUrl.toString()).willReturn("https://fake-presigned-url.com/image/test.jpg");
        given(presignedRequest.url()).willReturn(fakeUrl);
        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
            .willReturn(presignedRequest);

        // when
        String url = s3Storage.generatePresignedUrl(key);

        // then
        assertThat(url).isEqualTo("https://fake-presigned-url.com/image/test.jpg");
        ArgumentCaptor<GetObjectPresignRequest> captor = ArgumentCaptor.forClass(
            GetObjectPresignRequest.class);
        verify(s3Presigner).presignGetObject(captor.capture());
    }

    @Test
    void 파일_삭제_테스트() {

        // given
        String key = KEY + "test.jpg";

        // when
        s3Storage.deleteFile(key);

        // then
        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client, times(1)).deleteObject(captor.capture());
        DeleteObjectRequest req = captor.getValue();
        assertEquals("test-bucket", req.bucket());
        assertEquals(key, req.key());
    }

    @Test
    void presigned_url_실패시_S3Utilities_URL로_폴백() throws Exception {
        // given
        String key = "image/test.jpg";

        // 1) presign 실패 유도
        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
            .willThrow(new RuntimeException("presign failed"));

        // 2) utilities.getUrl 폴백 세팅
        S3Utilities utilities = mock(S3Utilities.class);
        given(s3Client.utilities()).willReturn(utilities);

        URL fakeUrl = new URL("https://test-bucket.s3.amazonaws.com/image/test.jpg");
        given(utilities.getUrl(any(GetUrlRequest.class))).willReturn(fakeUrl);

        // when
        String url = s3Storage.generatePresignedUrl(key);

        // then
        assertEquals("https://test-bucket.s3.amazonaws.com/image/test.jpg", url);
        verify(s3Presigner, times(1)).presignGetObject(any(GetObjectPresignRequest.class));
        verify(utilities, times(1)).getUrl(any(GetUrlRequest.class));
    }
}