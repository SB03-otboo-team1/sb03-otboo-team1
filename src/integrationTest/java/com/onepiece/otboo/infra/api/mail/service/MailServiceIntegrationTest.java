package com.onepiece.otboo.infra.api.mail.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;

import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.mail.MailHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ImportAutoConfiguration(exclude = MailHealthContributorAutoConfiguration.class)
@ActiveProfiles("test-integration")
public class MailServiceIntegrationTest {

    @Autowired
    private MailService mailService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void 임시비밀번호_이메일_비동기_발송_동작한다() throws Exception {
        int count = 3;
        MimeMessage mimeMessage = mock(MimeMessage.class);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        @SuppressWarnings("unchecked")
        CompletableFuture<Boolean>[] futures = new CompletableFuture[count];
        for (int i = 0; i < count; i++) {
            futures[i] = mailService.sendTemporaryPasswordEmail(
                "test" + i + "@example.com", "pw" + i, Instant.now().plusSeconds(600));
        }

        for (CompletableFuture<Boolean> future : futures) {
            assertTrue(future.get(1, TimeUnit.SECONDS));
        }

        verify(mailSender, times(count)).send(any(MimeMessage.class));
    }
}
