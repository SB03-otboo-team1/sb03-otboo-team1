package com.onepiece.otboo.infra.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;

class SpaCsrfTokenRequestHandlerTest {

    @Test
    void 헤더가_있으면_plain_없으면_xor_방식으로_분기한다() {
        // given
        CsrfTokenRequestHandler mockPlain = Mockito.mock(CsrfTokenRequestHandler.class);
        CsrfTokenRequestHandler mockXor = Mockito.mock(CsrfTokenRequestHandler.class);
        SpaCsrfTokenRequestHandler handler = new SpaCsrfTokenRequestHandler(mockPlain, mockXor);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        CsrfToken token = Mockito.mock(CsrfToken.class);
        Mockito.when(token.getHeaderName()).thenReturn("X-XSRF-TOKEN");

        // when: 헤더가 있으면 plain 핸들러 호출
        Mockito.when(request.getHeader("X-XSRF-TOKEN")).thenReturn("token-value");
        handler.resolveCsrfTokenValue(request, token);

        // then
        Mockito.verify(mockPlain).resolveCsrfTokenValue(request, token);
        Mockito.verify(mockXor, Mockito.never()).resolveCsrfTokenValue(request, token);

        // when: 헤더가 없으면 xor 핸들러 호출
        Mockito.when(request.getHeader("X-XSRF-TOKEN")).thenReturn(null);
        handler.resolveCsrfTokenValue(request, token);

        // then
        Mockito.verify(mockXor).resolveCsrfTokenValue(request, token);
    }

    @Test
    @SuppressWarnings("unchecked")
    void handle은_xor_핸들러를_항상_호출한다() {
        // given
        CsrfTokenRequestHandler mockPlain = Mockito.mock(CsrfTokenRequestHandler.class);
        CsrfTokenRequestHandler mockXor = Mockito.mock(CsrfTokenRequestHandler.class);
        SpaCsrfTokenRequestHandler handler = new SpaCsrfTokenRequestHandler(mockPlain, mockXor);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Supplier<CsrfToken> supplier = Mockito.mock(Supplier.class);

        // when
        handler.handle(request, response, supplier);

        // then
        Mockito.verify(mockXor).handle(request, response, supplier);
        Mockito.verify(supplier).get();
    }
}
