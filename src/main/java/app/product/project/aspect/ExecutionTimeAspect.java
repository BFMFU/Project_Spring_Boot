package app.product.project.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {

    @Around("execution(* app.product.project.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long end = System.nanoTime();
            long durationMs = (end - start) / 1_000_000;
            String signature = pjp.getSignature().toShortString();
            log.info("[TIMING] {} executed in {} ms", signature, durationMs);
        }
    }
}

