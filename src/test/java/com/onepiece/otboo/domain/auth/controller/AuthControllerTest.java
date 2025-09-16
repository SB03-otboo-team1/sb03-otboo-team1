package com.onepiece.otboo.domain.auth.controller;

import com.onepiece.otboo.infra.security.config.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

}
