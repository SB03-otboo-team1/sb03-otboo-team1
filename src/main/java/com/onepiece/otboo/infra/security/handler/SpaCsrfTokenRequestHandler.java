package com.onepiece.otboo.infra.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

public class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler plain;
    private final CsrfTokenRequestHandler xor;

    public SpaCsrfTokenRequestHandler() {
        this(new CsrfTokenRequestAttributeHandler(), new XorCsrfTokenRequestAttributeHandler());
    }

    public SpaCsrfTokenRequestHandler(CsrfTokenRequestHandler plain, CsrfTokenRequestHandler xor) {
        this.plain = plain;
        this.xor = xor;
    }

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        Supplier<CsrfToken> csrfToken
    ) {
        this.xor.handle(request, response, csrfToken);
        csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(
        HttpServletRequest request,
        CsrfToken csrfToken
    ) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());
        return (StringUtils.hasText(headerValue) ? this.plain : this.xor)
            .resolveCsrfTokenValue(request, csrfToken);
    }
}
