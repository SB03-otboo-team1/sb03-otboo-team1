package com.onepiece.otboo.infra.api.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ResourceLoader resourceLoader;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailService, "senderEmail", "test@otboo.com");
    }

    @Test
    void 임시_비밀번호_이메일_비동기_성공_true() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        AtomicReference<String> executingThreadName = new AtomicReference<>();
        // 실제 메일 전송 mock
        Mockito.doAnswer(invocation -> {
            executingThreadName.set(Thread.currentThread().getName());
            return null;
        }).when(mailSender).send(mimeMessage);

        CompletableFuture<Boolean> resultFuture = mailService.sendTemporaryPasswordEmail(
            "success@example.com",
            "temporaryPassword",
            Instant.now().plusSeconds(300)
        );

        Boolean result = resultFuture.get(3, TimeUnit.SECONDS);

        assertThat(result).isTrue();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void 임시_비밀번호_이메일_비동기_실패_false() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        Mockito.doThrow(new MailSendException("메일 발송 실패"))
            .when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Boolean> resultFuture = mailService.sendTemporaryPasswordEmail(
            "fail@example.com",
            "temporaryPassword",
            Instant.now().plusSeconds(300)
        );

        Boolean result = resultFuture.get(3, TimeUnit.SECONDS);

        assertThat(result).isFalse();
        verify(mailSender).send(any(MimeMessage.class));
    }
}
