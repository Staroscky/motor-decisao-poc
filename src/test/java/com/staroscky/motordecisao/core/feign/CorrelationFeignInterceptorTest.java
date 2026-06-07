package com.staroscky.motordecisao.core.feign;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationFeignInterceptorTest {

    private final CorrelationFeignInterceptor interceptor = new CorrelationFeignInterceptor();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void injectsBothHeadersWhenMdcIsPopulated() {
        MDC.put("correlationId", "abc-123");
        MDC.put("flowId", "flow-456");

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).containsKey("x-correlationId");
        assertThat(template.headers().get("x-correlationId")).containsExactly("abc-123");
        assertThat(template.headers()).containsKey("x-flowId");
        assertThat(template.headers().get("x-flowId")).containsExactly("flow-456");
    }

    @Test
    void doesNotInjectHeadersWhenMdcIsEmpty() {
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey("x-correlationId");
        assertThat(template.headers()).doesNotContainKey("x-flowId");
    }

    @Test
    void injectsOnlyCorrelationIdWhenFlowIdIsAbsent() {
        MDC.put("correlationId", "abc-123");

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).containsKey("x-correlationId");
        assertThat(template.headers()).doesNotContainKey("x-flowId");
    }
}
