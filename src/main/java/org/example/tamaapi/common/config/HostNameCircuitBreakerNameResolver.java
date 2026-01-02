package org.example.tamaapi.common.config;

import feign.Target;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

@Component
@Slf4j
public class HostNameCircuitBreakerNameResolver implements CircuitBreakerNameResolver {
    @Override
    public String resolveCircuitBreakerName(String feignClientName, Target<?> target, Method method) {
        String url = target.url();
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            log.error("URL 객체 생성 중 예외 발생: {}", e.getMessage());
            return "default";
        }
    }
}
