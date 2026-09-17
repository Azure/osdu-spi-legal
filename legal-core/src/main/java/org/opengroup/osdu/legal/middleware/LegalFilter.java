package org.opengroup.osdu.legal.middleware;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.opengroup.osdu.core.common.http.ResponseHeadersFactory;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.Request;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class LegalFilter implements Filter {

    @Inject
    private IAuthorizationService authorizationServiceEntitlements;

    @Inject
    private DpsHeaders headers;

    @Inject
    private RequestInfo requestInfo;

    @Inject
    private JaxRsDpsLog logger;

    private ResponseHeadersFactory responseHeadersFactory = new ResponseHeadersFactory();

    // defaults to * for any front-end, string must be comma-delimited if more than one domain
    @Value("${ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS:*}")
    String ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS;

    @Value("${ACCEPT_HTTP:false}")
    private boolean acceptHttp;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        headers.addCorrelationIdIfMissing();
        long startTime = System.currentTimeMillis();
        setResponseHeaders(httpServletResponse);
        try {
            if (!validateIsHttps(httpServletResponse,httpServletRequest)) {
                //do nothing
            } else if (isOptionsMethod(httpServletRequest)) {
                httpServletResponse.setStatus(200);
            } else {
                chain.doFilter(request, response);
            }
        }finally {
            logRequest(httpServletRequest, httpServletResponse, startTime);
        }

	}

	@Override
	public void destroy() {
    }

    private boolean validateIsHttps( HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest) {
        String uri = requestInfo.getUri();
        if (!isLocalHost(uri)
            && !isCronJob(uri)
            && !isSwagger(uri)
            && !isVersionInfo(uri)
            && !isHealthCheck(uri)
            && !isOptionsMethod(httpServletRequest)) {
            if(!hasJwt()) {
                httpServletResponse.setStatus(401);
                return false;
            }
            else if(!isAcceptHttp() && !requestInfo.isHttps()) {
                String location = uri.replaceFirst("http", "https");
                httpServletResponse.setStatus(307);
                httpServletResponse.addHeader("location", location);
                return false;
            }
        }
        return true;
    }

    private boolean hasJwt() {
        String authorization = headers.getAuthorization();
        return (authorization != null) && (authorization.length() > 0);
    }

    private boolean isLocalHost(String uri) {
        return (uri.contains("//localhost") || uri.contains("//127.0.0.1"));
    }

    private boolean isHealthCheck(String uri) {
        return (uri.endsWith("/liveness_check") || uri.endsWith("/readiness_check"));
    }

    private boolean isCronJob(String uri) {
        return uri.contains("/jobs/updateLegalTagStatus");
    }

    private boolean isVersionInfo(String uri) {
        return uri.contains("/info");
    }

    private boolean isSwagger(String uri) {
        return uri.contains("/swagger") || uri.contains("api-docs");
    }

    private boolean isOptionsMethod(HttpServletRequest httpServletRequest){
        return httpServletRequest.getMethod().equalsIgnoreCase("OPTIONS");
    }

    private void logRequest(HttpServletRequest servletRequest, HttpServletResponse servletResponse, long startTime) {
        String uri = requestInfo.getUri();
        if(!isHealthCheck(uri)) {
            logger.request(Request.builder()
                    .requestMethod(servletRequest.getMethod())
                    .latency(Duration.ofMillis(System.currentTimeMillis() - startTime))
                    .requestUrl(uri)
                    .Status(servletResponse.getStatus())
                    .ip(servletRequest.getRemoteAddr())
                    .build());
        }
    }

    private void setResponseHeaders(HttpServletResponse httpServletResponse) {
        Map<String, String> responseHeaders = responseHeadersFactory.getResponseHeaders(ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS);
        for(Map.Entry<String, String> header : responseHeaders.entrySet()){
            httpServletResponse.addHeader(header.getKey(), header.getValue().toString());
        }
        httpServletResponse.addHeader(DpsHeaders.CORRELATION_ID, this.headers.getCorrelationId());
    }

    public boolean isAcceptHttp() {
        return acceptHttp;
    }
}