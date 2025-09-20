package com.onepiece.otboo;

import com.onepiece.otboo.infra.security.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class OtbooApplicationTests {

    @Test
    void contextLoads() {
    }

}
