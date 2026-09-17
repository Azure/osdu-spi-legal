package org.opengroup.osdu.legal.middleware;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.Request;
import org.opengroup.osdu.core.common.model.http.RequestInfo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalFilterTest {

    @Mock
    private DpsHeaders headers;

    @Mock
    private RequestInfo requestInfo;

    @Mock
    private JaxRsDpsLog logger;

    @InjectMocks
    private LegalFilter legalFilter;

    @Test
    public void shouldSetCorrectResponseHeaders() throws IOException, ServletException {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        Mockito.when(requestInfo.getUri()).thenReturn("https://test.com");
        Mockito.when(requestInfo.isHttps()).thenReturn(true);
        Mockito.when(headers.getAuthorization()).thenReturn("authorization-header-value");
        Mockito.when(headers.getCorrelationId()).thenReturn("correlation-id-value");
        Mockito.when(httpServletRequest.getMethod()).thenReturn("POST");
        org.springframework.test.util.ReflectionTestUtils.setField(legalFilter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");

        legalFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        Mockito.verify(httpServletResponse).addHeader("Access-Control-Allow-Origin", "custom-domain");
        Mockito.verify(httpServletResponse).addHeader("Access-Control-Allow-Headers", "access-control-allow-origin, origin, content-type, accept, authorization, data-partition-id, correlation-id, appkey");
        Mockito.verify(httpServletResponse).addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        Mockito.verify(httpServletResponse).addHeader("Access-Control-Allow-Credentials", "true");
        Mockito.verify(httpServletResponse).addHeader("X-Frame-Options", "DENY");
        Mockito.verify(httpServletResponse).addHeader("X-XSS-Protection", "1; mode=block");
        Mockito.verify(httpServletResponse).addHeader("X-Content-Type-Options", "nosniff");
        Mockito.verify(httpServletResponse).addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        Mockito.verify(httpServletResponse).addHeader("Content-Security-Policy", "default-src 'self'");
        Mockito.verify(httpServletResponse).addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        Mockito.verify(httpServletResponse).addHeader("Expires", "0");
        Mockito.verify(httpServletResponse).addHeader("correlation-id", "correlation-id-value");
        Mockito.verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
        Mockito.verify(logger).request(Mockito.any(Request.class));
    }
}
