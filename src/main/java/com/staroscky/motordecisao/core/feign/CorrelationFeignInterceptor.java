package com.staroscky.motordecisao.core.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CorrelationFeignInterceptor implements RequestInterceptor {

    private static final String X_CORRELATION_ID = "x-correlationId";
    private static final String X_FLOW_ID        = "x-flowId";

    @Override
    public void apply(RequestTemplate template) {
        addIfPresent(template, X_CORRELATION_ID, MDC.get("correlationId"));
        addIfPresent(template, X_FLOW_ID,        MDC.get("flowId"));
    }

    private void addIfPresent(RequestTemplate template, String header, String value) {
        if (StringUtils.hasText(value)) {
            template.header(header, value);
        }
    }
}
