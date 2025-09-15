package com.onepiece.otboo.infra.security.fixture;

import com.nimbusds.jose.JOSEException;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetailsService;
import org.mockito.Mockito;

public class JwtProviderFixture {

    public static JwtProvider create() throws JOSEException {
        String accessSecret = "c2VjdXJlc2VjdXJlc2VjdXJlc2VjdXJlc2VjdXJlc2VjdXJlc2VjdXJlcg==";
        String refreshSecret = "cmVmcmVzaHJlZnJlc2hyZWZyZXNocmVmcmVzaHJlZnJlc2hyZWZyZXNo";
        int accessTokenExpirationMs = 1000;
        int refreshTokenExpirationMs = 1000;

        CustomUserDetailsService userDetailsService = Mockito.mock(CustomUserDetailsService.class);
        JwtRegistry jwtRegistry = Mockito.mock(JwtRegistry.class);

        return new JwtProvider(
            accessSecret,
            accessTokenExpirationMs,
            refreshSecret,
            refreshTokenExpirationMs,
            userDetailsService,
            jwtRegistry
        );
    }
}
