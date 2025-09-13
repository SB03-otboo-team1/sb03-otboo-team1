package com.onepiece.otboo.infra.security.jwt;

import java.util.UUID;

public interface JwtRegistry {

    void invalidateAllTokens(UUID userId);
}
