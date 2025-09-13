package com.onepiece.otboo.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(
        @RequestParam("username") String username,
        @RequestParam("password") String password
    ) {
        // TODO: 실제 구현은 이후에 작성
        return ResponseEntity.status(501).body(null);
    }
}
