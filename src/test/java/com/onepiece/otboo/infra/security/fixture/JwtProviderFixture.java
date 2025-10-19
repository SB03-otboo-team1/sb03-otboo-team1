package com.onepiece.otboo.infra.security.fixture;

import com.nimbusds.jose.JOSEException;
import com.onepiece.otboo.infra.security.jwt.JwtProvider;
import com.onepiece.otboo.infra.security.jwt.JwtRegistry;
import com.onepiece.otboo.infra.security.userdetails.CustomUserDetailsService;

public class JwtProviderFixture {

    public static JwtProvider create(CustomUserDetailsService userDetailsService,
        JwtRegistry jwtRegistry) throws JOSEException {
        String accessSecret = "c2VjdXJlc2VjdXJlc2VjdXJlc2VjdXJlc2VjdXJlc2VjdXJlc2VjdXJlcg==";
        String refreshSecret = "cmVmcmVzaHJlZnJlc2hyZWZyZXNocmVmcmVzaHJlZnJlc2hyZWZyZXNo";
        int accessTokenExpirationMs = 1000 * 60 * 10;
        int refreshTokenExpirationMs = 1000 * 60 * 60;
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
