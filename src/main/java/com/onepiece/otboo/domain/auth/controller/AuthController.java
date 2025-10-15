package com.onepiece.otboo.domain.auth.controller;

import com.onepiece.otboo.domain.auth.controller.api.AuthApi;
import com.onepiece.otboo.domain.auth.dto.request.ResetPasswordRequest;
import com.onepiece.otboo.domain.auth.dto.request.SignInRequest;
import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.domain.auth.service.AuthService;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.domain.user.exception.UserNotFoundException;
import com.onepiece.otboo.domain.user.repository.UserRepository;
import com.onepiece.otboo.infra.api.mail.service.MailService;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final MailService mailService;
    private final UserRepository userRepository;

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @Override
    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping(value = "/sign-in", consumes = "multipart/form-data")
    public void signIn(@ModelAttribute SignInRequest signInRequest) {
    }

    @Override
    @PostMapping("/sign-out")
    public void signOut() {
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refreshToken(
        @CookieValue("REFRESH_TOKEN") String refreshToken,
        HttpServletResponse response
    ) {
        var refreshTokenData = authService.refreshToken(refreshToken);
        Cookie refreshCookie = jwtProvider.generateRefreshTokenCookie(
            refreshTokenData.newRefreshToken());
        response.addCookie(refreshCookie);
        return ResponseEntity.ok(refreshTokenData.jwtDto());
    }

    @Override
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            String tempPassword = authService.saveTemporaryPassword(request.email());
            User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> UserNotFoundException.byEmail(request.email()));
            CompletableFuture<Boolean> mailResult = mailService.sendTemporaryPasswordEmail(
                request.email(),
                tempPassword,
                user.getTemporaryPasswordExpirationTime()
            );
            mailResult.whenComplete((success, throwable) -> {
                if (throwable != null) {
                    log.error("임시 비밀번호 이메일 발송 중 예외 발생 - 수신자: {}", request.email(),
                        throwable);
                    return;
                }

                if (Boolean.FALSE.equals(success)) {
                    log.warn("임시 비밀번호 이메일 발송에 실패했습니다 - 수신자: {}", request.email());
                }
            });
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
