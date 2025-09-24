package com.onepiece.otboo.infra.security.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityChainOrder {

    public static final int SWAGGER = 0;
    public static final int H2_CONSOLE = 1;
    public static final int APPLICATION = 2;
    public static final int OAUTH2_CLIENT = 3;
}
