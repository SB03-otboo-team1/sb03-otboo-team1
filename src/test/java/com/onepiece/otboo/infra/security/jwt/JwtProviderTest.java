package com.onepiece.otboo.infra.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JOSEException;
import com.onepiece.otboo.infra.security.fixture.JwtProviderFixture;
import com.onepiece.otboo.infra.security.fixture.UserDetailsFixture;
import java.util.Date;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    @Test
    void 잘못된_토큰은_검증에_실패한다() throws JOSEException {
        JwtProvider jwtProvider = JwtProviderFixture.create();
        String invalidToken = JwtTestHelper.createInvalidToken();
        boolean result = jwtProvider.validateAccessToken(invalidToken);
        assertThat(result).isFalse();
    }

    @Test
    void 만료된_토큰은_검증에_실패한다() throws Exception {
        JwtProvider jwtProvider = JwtProviderFixture.create();
        Date expired = new Date(System.currentTimeMillis() - 10000);
        var userDetails = UserDetailsFixture.createUser();
        String token = JwtTestHelper.createAccessTokenWithExpiry(jwtProvider, userDetails, expired);
        boolean result = jwtProvider.validateAccessToken(token);
        assertThat(result).isFalse();
    }

    @Test
    void 정상_토큰은_검증에_성공한다() throws Exception {
        JwtProvider jwtProvider = JwtProviderFixture.create();
        var userDetails = UserDetailsFixture.createUser();
        String token = jwtProvider.generateAccessToken(userDetails);
        boolean result = jwtProvider.validateAccessToken(token);
        assertThat(result).isTrue();
    }
}
