package org.example.tamaapi;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableRetry
@EnableBatchProcessing
@EnableFeignClients
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@RequiredArgsConstructor
@Slf4j
public class TamaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TamaApiApplication.class, args);
    }

    private final TimeLimiterRegistry timeLimiterRegistry;
    @PostConstruct
    public void logTimeLimiterConfig() {
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("localhost");

        log.info("=== TimeLimiter[localhost] Config ===");
        log.info("timeoutDuration = {} ms",
                timeLimiter.getTimeLimiterConfig()
                        .getTimeoutDuration()
                        .toMillis());
        log.info("cancelRunningFuture = {}",
                timeLimiter.getTimeLimiterConfig()
                        .shouldCancelRunningFuture());
    }
}
