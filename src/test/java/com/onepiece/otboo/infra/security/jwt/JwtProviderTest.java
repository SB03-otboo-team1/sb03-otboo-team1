package com.onepiece.otboo.infra.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.JOSEException;
import com.onepiece.otboo.infra.security.fixture.JwtProviderFixture;
import com.onepiece.otboo.infra.security.fixture.UserDetailsFixture;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetails;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetailsService;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private JwtRegistry jwtRegistry;

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() throws JOSEException {
        jwtProvider = JwtProviderFixture.create(userDetailsService, jwtRegistry);
    }

    @Test
    void 블랙리스트_등록된_토큰은_검증에_실패한다() throws Exception {
        CustomUserDetails userDetails = UserDetailsFixture.createUser();
        String token = jwtProvider.generateAccessToken(userDetails);
        String jti = jwtProvider.getTokenId(token);
        when(jwtRegistry.isBlacklisted(jti)).thenReturn(true);

        boolean result = jwtProvider.validateAccessToken(token);

        assertThat(result).isFalse();
    }

    @Test
    void getEmailFromToken_정상_및_비정상() throws Exception {
        CustomUserDetails userDetails = UserDetailsFixture.createUser();
        String token = jwtProvider.generateAccessToken(userDetails);

        assertThat(jwtProvider.getEmailFromToken(token)).isEqualTo(userDetails.getUsername());
        assertThat(jwtProvider.getEmailFromToken("invalid.token")).isNull();
    }

    @Test
    void getTokenId_정상_및_비정상() throws Exception {
        CustomUserDetails userDetails = UserDetailsFixture.createUser();
        String token = jwtProvider.generateAccessToken(userDetails);

        assertThat(jwtProvider.getTokenId(token)).isNotNull();
        assertThat(jwtProvider.getTokenId("invalid.token")).isNull();
    }

    @Test
    void getUserIdFromToken_정상_및_비정상() throws Exception {
        CustomUserDetails userDetails = UserDetailsFixture.createUser();
        when(userDetailsService.loadUserByUsername(userDetails.getUsername())).thenReturn(
            userDetails);
        String token = jwtProvider.generateAccessToken(userDetails);

        assertThat(jwtProvider.getUserIdFromToken(token)).isEqualTo(userDetails.getUserId());
        assertThat(jwtProvider.getUserIdFromToken("invalid.token")).isNull();
    }

    @Test
    void getExpirationInstant_정상_및_비정상() throws Exception {
        CustomUserDetails userDetails = UserDetailsFixture.createUser();
        String token = jwtProvider.generateAccessToken(userDetails);

        assertThat(jwtProvider.getExpirationInstant(token)).isNotNull();
        assertThat(jwtProvider.getExpirationInstant("invalid.token")).isNull();
    }

    @Test
    void generateRefreshTokenCookie_속성_확인() throws Exception {
        CustomUserDetails userDetails = UserDetailsFixture.createUser();
        String refreshToken = jwtProvider.generateRefreshToken(userDetails);
        var cookie = jwtProvider.generateRefreshTokenCookie(refreshToken);

        assertThat(cookie.getName()).isEqualTo(JwtProvider.REFRESH_TOKEN_COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo(refreshToken);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isGreaterThan(0);
    }

    @Test
    void generateRefreshTokenExpirationCookie_속성_확인() throws Exception {
        var cookie = jwtProvider.generateRefreshTokenExpirationCookie();

        assertThat(cookie.getName()).isEqualTo(JwtProvider.REFRESH_TOKEN_COOKIE_NAME);
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isZero();
    }

    @Test
    void getAuthentication_정상_및_비정상() throws Exception {
        CustomUserDetails userDetails = UserDetailsFixture.createUser();
        when(userDetailsService.loadUserByUsername(userDetails.getUsername())).thenReturn(
            userDetails);
        String token = jwtProvider.generateAccessToken(userDetails);

        assertThat(jwtProvider.getAuthentication(token)).isNotNull();
        assertThat(jwtProvider.getAuthentication("invalid.token")).isNull();
    }

    @Test
    void 잘못된_토큰은_검증에_실패한다() throws Exception {
        String invalidToken = JwtTestHelper.createInvalidToken();
        boolean result = jwtProvider.validateAccessToken(invalidToken);

        assertThat(result).isFalse();
    }

    @Test
    void 만료된_토큰은_검증에_실패한다() throws Exception {
        CustomUserDetails userDetails = UserDetailsFixture.createUser();
        Date expired = new Date(System.currentTimeMillis() - 10000);
        String token = JwtTestHelper.createAccessTokenWithExpiry(jwtProvider, userDetails, expired);
        boolean result = jwtProvider.validateAccessToken(token);

        assertThat(result).isFalse();
    }

    @Test
    void 정상_토큰은_검증에_성공한다() throws Exception {
        CustomUserDetails userDetails = UserDetailsFixture.createUser();
        String token = jwtProvider.generateAccessToken(userDetails);
        boolean result = jwtProvider.validateAccessToken(token);

        assertThat(result).isTrue();
    }
}
