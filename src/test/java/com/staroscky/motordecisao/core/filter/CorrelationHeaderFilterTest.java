package com.staroscky.motordecisao.core.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationHeaderFilterTest {

    private final CorrelationHeaderFilter filter = new CorrelationHeaderFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void populatesMdcAndResponseHeadersWhenBothHeadersPresent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-correlationId", "abc-123");
        request.addHeader("x-flowId", "flow-456");

        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcCorrelation = new AtomicReference<>();
        AtomicReference<String> mdcFlow = new AtomicReference<>();

        filter.doFilterInternal(request, response, (req, res) -> {
            mdcCorrelation.set(MDC.get("correlationId"));
            mdcFlow.set(MDC.get("flowId"));
        });

        assertThat(mdcCorrelation.get()).isEqualTo("abc-123");
        assertThat(mdcFlow.get()).isEqualTo("flow-456");
        assertThat(response.getHeader("x-correlationId")).isEqualTo("abc-123");
        assertThat(response.getHeader("x-flowId")).isEqualTo("flow-456");
    }

    @Test
    void clearsMdcAfterFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-correlationId", "abc-123");
        request.addHeader("x-flowId", "flow-456");

        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(MDC.get("correlationId")).isNull();
        assertThat(MDC.get("flowId")).isNull();
    }

    @Test
    void doesNotPopulateMdcWhenHeadersAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        AtomicReference<String> mdcCorrelation = new AtomicReference<>();
        AtomicReference<String> mdcFlow = new AtomicReference<>();

        filter.doFilterInternal(request, new MockHttpServletResponse(), (req, res) -> {
            mdcCorrelation.set(MDC.get("correlationId"));
            mdcFlow.set(MDC.get("flowId"));
        });

        assertThat(mdcCorrelation.get()).isNull();
        assertThat(mdcFlow.get()).isNull();
    }

    @Test
    void doesNotPopulateMdcWhenHeaderIsEmpty() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-correlationId", "");
        AtomicReference<String> mdcCorrelation = new AtomicReference<>();

        filter.doFilterInternal(request, new MockHttpServletResponse(), (req, res) -> {
            mdcCorrelation.set(MDC.get("correlationId"));
        });

        assertThat(mdcCorrelation.get()).isNull();
    }

    @Test
    void doesNotLeakMdcBetweenSequentialRequests() throws Exception {
        MockHttpServletRequest firstRequest = new MockHttpServletRequest();
        firstRequest.addHeader("x-correlationId", "req-1");
        filter.doFilterInternal(firstRequest, new MockHttpServletResponse(), new MockFilterChain());

        MockHttpServletRequest secondRequest = new MockHttpServletRequest();
        AtomicReference<String> mdcDuringSecond = new AtomicReference<>();

        filter.doFilterInternal(secondRequest, new MockHttpServletResponse(), (req, res) -> {
            mdcDuringSecond.set(MDC.get("correlationId"));
        });

        assertThat(mdcDuringSecond.get()).isNull();
    }
}
