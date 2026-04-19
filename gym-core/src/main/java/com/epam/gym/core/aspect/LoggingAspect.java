package com.epam.gym.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
    private static final Set<String> SENSITIVE_PARAM_NAMES = Set.of(
            "password", "currentpassword", "newpassword", "oldpassword",
            "secret", "token", "accesstoken", "refreshtoken", "apikey", "pwd", "pass"
    );

    @Around("@within(com.epam.gym.core.aspect.LogExecution) || @annotation(com.epam.gym.core.aspect.LogExecution)")
    public Object logServiceMethod(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        log.debug("Calling: {} args: {}", method, Arrays.toString(sanitizeArgs(pjp)));
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

    private Object[] sanitizeArgs(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        if (args == null) {
            return new Object[0];
        }
        String[] parameterNames = null;
        if (pjp.getSignature() instanceof MethodSignature methodSignature) {
            parameterNames = methodSignature.getParameterNames();
        }

        Object[] sanitized = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            String parameterName = parameterNames != null && i < parameterNames.length && parameterNames[i] != null
                    ? parameterNames[i].toLowerCase()
                    : "";
            sanitized[i] = SENSITIVE_PARAM_NAMES.stream().anyMatch(parameterName::contains)
                    ? "[REDACTED]"
                    : arg;
        }
        return sanitized;
    }
}
