package com.staroscky.motordecisao.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationHeaderFilter extends OncePerRequestFilter {

    private static final String X_CORRELATION_ID = "x-correlationId";
    private static final String X_FLOW_ID        = "x-flowId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            setIfPresent(request, response, X_CORRELATION_ID, "correlationId");
            setIfPresent(request, response, X_FLOW_ID,        "flowId");
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("correlationId");
            MDC.remove("flowId");
        }
    }

    private void setIfPresent(HttpServletRequest request, HttpServletResponse response,
                               String header, String mdcKey) {
        String value = request.getHeader(header);
        if (StringUtils.hasText(value)) {
            MDC.put(mdcKey, value);
            response.addHeader(header, value);
        }
    }
}
