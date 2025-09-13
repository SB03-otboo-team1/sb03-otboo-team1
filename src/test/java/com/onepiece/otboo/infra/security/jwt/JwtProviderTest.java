package com.onepiece.otboo.infra.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JOSEException;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;

class JwtProviderTest {

    private final String accessSecret = "c2VjdXJlc2VjdXJlc2VjdXJlc2VjdXJlc2VjdXJlc2VjdXJlc2VjdXJlcg==";
    private final String refreshSecret = "cmVmcmVzaHJlZnJlc2hyZWZyZXNocmVmcmVzaHJlZnJlc2hyZWZyZXNo";
    private final int accessTokenExpirationMs = 1000;
    private final int refreshTokenExpirationMs = 1000;

    private final UserDetailsService userDetailsService = username -> null;

    @Test
    void 잘못된_토큰은_검증에_실패한다() throws JOSEException {
        JwtProvider jwtProvider = new JwtProvider(
            accessSecret,
            accessTokenExpirationMs,
            refreshSecret,
            refreshTokenExpirationMs,
            userDetailsService
        );
        String invalidToken = JwtTestHelper.createInvalidToken();
        boolean result = jwtProvider.validateAccessToken(invalidToken);
        assertThat(result).isFalse();
    }

    @Test
    void 만료된_토큰은_검증에_실패한다() throws Exception {
        JwtProvider jwtProvider = new JwtProvider(
            accessSecret,
            accessTokenExpirationMs,
            refreshSecret,
            refreshTokenExpirationMs,
            userDetailsService
        );
        Date expired = new Date(System.currentTimeMillis() - 10000);
        var userDetails = JwtTestHelper.createUser("testuser@email.com", "password", List.of());
        String token = JwtTestHelper.createAccessTokenWithExpiry(jwtProvider, userDetails, expired);
        boolean result = jwtProvider.validateAccessToken(token);
        assertThat(result).isFalse();
    }

    @Test
    void 정상_토큰은_검증에_성공한다() throws Exception {
        JwtProvider jwtProvider = new JwtProvider(
            accessSecret,
            accessTokenExpirationMs,
            refreshSecret,
            refreshTokenExpirationMs,
            userDetailsService
        );
        var userDetails = JwtTestHelper.createUser("testuser@email.com", "password", List.of());
        String token = jwtProvider.generateAccessToken(userDetails);
        boolean result = jwtProvider.validateAccessToken(token);
        assertThat(result).isTrue();
    }
}
