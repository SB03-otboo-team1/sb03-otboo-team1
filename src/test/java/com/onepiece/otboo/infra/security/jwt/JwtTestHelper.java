package com.onepiece.otboo.infra.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtTestHelper {

    public static String createAccessTokenWithExpiry(JwtProvider provider, UserDetails userDetails,
        Date expiryDate) throws JOSEException {
        String tokenId = UUID.randomUUID().toString();
        String username = userDetails.getUsername();
        Date now = new Date();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(username)
            .jwtID(tokenId)
            .claim("type", "access")
            .claim("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()))
            .issueTime(now)
            .expirationTime(expiryDate)
            .build();
        SignedJWT signedJWT = new SignedJWT(
            new JWSHeader(JWSAlgorithm.HS256),
            claimsSet
        );
        signedJWT.sign(provider.accessTokenSigner);
        return signedJWT.serialize();
    }

    public static UserDetails createUser(String email, String password, List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
            .map(role -> (GrantedAuthority) () -> role)
            .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(email, password, authorities);
    }

    public static String createInvalidToken() {
        return "invalid.token.value";
    }
}
