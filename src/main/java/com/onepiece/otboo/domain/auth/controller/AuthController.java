package com.onepiece.otboo.domain.auth.controller;


import com.onepiece.otboo.domain.auth.controller.api.AuthApi;
import com.onepiece.otboo.domain.auth.dto.request.SignInRequest;
import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.domain.auth.service.AuthService;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @PostMapping(
        path = "/sign-in",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE},
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<JwtDto> signIn(@Valid SignInRequest request) {
        JwtDto jwtDto = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(jwtDto);
    }

    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/refresh")
    public ResponseEntity<JwtDto> refreshToken(
        @CookieValue("REFRESH_TOKEN") String refreshToken,
        HttpServletResponse response
    ) {
        JwtDto jwtDto = authService.refreshToken(refreshToken);
        Cookie refreshCookie = jwtProvider.generateRefreshTokenCookie(refreshToken);
        response.addCookie(refreshCookie);
        return ResponseEntity.ok(jwtDto);
    }
}
