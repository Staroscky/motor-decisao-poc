package com.staroscky.motordecisao.core.mdc;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (mdcSnapshot != null) {
                    MDC.setContextMap(mdcSnapshot);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
