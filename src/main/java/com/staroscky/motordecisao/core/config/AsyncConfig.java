package com.staroscky.motordecisao.core.config;

import com.staroscky.motordecisao.core.mdc.MdcTaskDecorator;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private final SimpleAsyncTaskExecutorBuilder executorBuilder;

    public AsyncConfig(SimpleAsyncTaskExecutorBuilder executorBuilder) {
        this.executorBuilder = executorBuilder;
    }

    @Override
    public Executor getAsyncExecutor() {
        return executorBuilder
            .taskDecorator(new MdcTaskDecorator())
            .build();
    }
}
