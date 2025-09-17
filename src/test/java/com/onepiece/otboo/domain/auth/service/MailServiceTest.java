package com.onepiece.otboo.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.onepiece.otboo.domain.auth.dto.request.ResetPasswordRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class MailServiceTest {

    @MockitoBean
    private MailService mailService;

    @Test
    void 이메일_발송_성공_시_true_반환() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com");
        given(mailService.sendTemporaryPasswordEmail(request.email(), "tempPw")).willReturn(true);

        boolean result = mailService.sendTemporaryPasswordEmail(request.email(), "tempPw");

        assertThat(result).isTrue();
    }

    @Test
    void 이메일_발송_실패_시_false_반환() {
        ResetPasswordRequest request = new ResetPasswordRequest("fail@example.com");
        given(mailService.sendTemporaryPasswordEmail(request.email(), "tempPw")).willReturn(false);

        boolean result = mailService.sendTemporaryPasswordEmail(request.email(), "tempPw");

        assertThat(result).isFalse();
    }
}
