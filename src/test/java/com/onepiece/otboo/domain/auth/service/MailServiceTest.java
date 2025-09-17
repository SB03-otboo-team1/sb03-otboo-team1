package com.onepiece.otboo.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.onepiece.otboo.domain.auth.dto.request.ResetPasswordRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class MailServiceTest {

    private final MailService mailService = mock(MailService.class);

    @Test
    void 이메일_발송_성공_시_true_반환() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com");
        var expirationTime = java.time.LocalDateTime.now().plusMinutes(3);
        given(mailService.sendTemporaryPasswordEmail(request.email(), "tempPw",
            expirationTime)).willReturn(true);

        boolean result = mailService.sendTemporaryPasswordEmail(request.email(), "tempPw",
            expirationTime);

        assertThat(result).isTrue();
    }

    @Test
    void 이메일_발송_실패_시_false_반환() {
        ResetPasswordRequest request = new ResetPasswordRequest("fail@example.com");
        var expirationTime = java.time.LocalDateTime.now().plusMinutes(3);
        given(mailService.sendTemporaryPasswordEmail(request.email(), "tempPw",
            expirationTime)).willReturn(false);

        boolean result = mailService.sendTemporaryPasswordEmail(request.email(), "tempPw",
            expirationTime);

        assertThat(result).isFalse();
    }
}
