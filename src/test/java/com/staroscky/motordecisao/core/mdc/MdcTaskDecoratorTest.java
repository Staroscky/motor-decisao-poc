package com.staroscky.motordecisao.core.mdc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MdcTaskDecoratorTest {

    private final MdcTaskDecorator decorator = new MdcTaskDecorator();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void propagatesMdcSnapshotToDecoratedRunnable() throws Exception {
        MDC.put("correlationId", "abc-123");
        MDC.put("flowId", "flow-456");

        AtomicReference<String> capturedCorrelation = new AtomicReference<>();
        AtomicReference<String> capturedFlow = new AtomicReference<>();

        Runnable decorated = decorator.decorate(() -> {
            capturedCorrelation.set(MDC.get("correlationId"));
            capturedFlow.set(MDC.get("flowId"));
        });

        Thread virtualThread = Thread.ofVirtual().start(decorated);
        virtualThread.join();

        assertThat(capturedCorrelation.get()).isEqualTo("abc-123");
        assertThat(capturedFlow.get()).isEqualTo("flow-456");
    }

    @Test
    void clearsMdcAfterExecution() throws Exception {
        MDC.put("correlationId", "abc-123");

        AtomicReference<String> mdcAfterExecution = new AtomicReference<>();

        Runnable decorated = decorator.decorate(() -> {});

        Thread virtualThread = Thread.ofVirtual().start(() -> {
            decorated.run();
            mdcAfterExecution.set(MDC.get("correlationId"));
        });
        virtualThread.join();

        assertThat(mdcAfterExecution.get()).isNull();
    }

    @Test
    void doesNotThrowWhenMdcIsEmpty() {
        assertThatCode(() -> {
            Runnable decorated = decorator.decorate(() -> {});
            decorated.run();
        }).doesNotThrowAnyException();
    }
}
