package com.epam.gym.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@within(com.epam.gym.core.aspect.LogExecution) || @annotation(com.epam.gym.core.aspect.LogExecution)")
    public Object logServiceMethod(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        log.debug("Calling: {} args: {}", method, Arrays.toString(pjp.getArgs()));
        try {
            Object result = pjp.proceed();
            log.debug("Completed: {}", method);
            return result;
        } catch (Exception e) {
            log.error("Failed {}: {}", method, e.getMessage());
            log.debug("Stack trace:", e);
            throw e;
        }
    }
}
