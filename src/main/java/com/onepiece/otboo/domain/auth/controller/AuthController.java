package com.onepiece.otboo.domain.auth.controller;


import com.onepiece.otboo.domain.auth.controller.api.AuthApi;
import com.onepiece.otboo.domain.auth.dto.request.SignInRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

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
}
