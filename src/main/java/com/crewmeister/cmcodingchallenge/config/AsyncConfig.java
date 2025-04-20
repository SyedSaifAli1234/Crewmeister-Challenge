package com.crewmeister.cmcodingchallenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "exchangeRateTaskExecutor")
    public Executor exchangeRateTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);  // Increased from default
        executor.setMaxPoolSize(20);   // Increased from default
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ExchangeRate-");
        executor.initialize();
        return executor;
    }
} 