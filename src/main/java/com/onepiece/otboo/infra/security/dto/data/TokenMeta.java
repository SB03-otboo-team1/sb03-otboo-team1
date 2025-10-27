package com.onepiece.otboo.infra.security.dto.data;

import java.time.Instant;
import java.util.UUID;

public record TokenMeta(UUID userId, Instant expiresAt) {

}
