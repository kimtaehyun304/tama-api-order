package org.example.tamaapi.common.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LogExecutionTimeAspect {

    @Around("@annotation(org.example.tamaapi.common.aspect.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            return joinPoint.proceed(); // 실제 메서드 실행
        } finally {
            long end = System.currentTimeMillis();
            long elapsed = end - start;
            log.info("{} 실행 시간: {} ms", joinPoint.getSignature(), elapsed);
        }
    }

}
