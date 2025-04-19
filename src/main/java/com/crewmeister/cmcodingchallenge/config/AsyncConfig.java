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
        executor.setCorePoolSize(4); // Same as previous PARALLEL_THREADS
        executor.setMaxPoolSize(8);  // Allow growth if needed
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("ExchangeRate-");
        executor.initialize();
        return executor;
    }
} 