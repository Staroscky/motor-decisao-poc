package com.staroscky.motordecisao.core.config;

import feign.Logger;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.openfeign.FeignLogbookLogger;

@Configuration
@EnableFeignClients(basePackages = "com.staroscky.motordecisao")
public class FeignConfig {

    @Bean
    public Logger feignLogger(Logbook logbook) {
        return new FeignLogbookLogger(logbook);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
