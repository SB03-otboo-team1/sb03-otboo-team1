package com.onepiece.otboo.domain.auth.controller;


import com.onepiece.otboo.domain.auth.controller.api.AuthApi;
import com.onepiece.otboo.domain.auth.dto.request.SignInRequest;
import com.onepiece.otboo.domain.auth.dto.response.JwtDto;
import com.onepiece.otboo.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;

    @PostMapping("/sign-in")
    public ResponseEntity<JwtDto> signIn(@ModelAttribute SignInRequest request) {
        JwtDto jwtDto = authService.login(request.username(), request.password());
        return ResponseEntity.ok(jwtDto);
    }
}
