package com.epam.gym.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Level 1 logging — transaction boundary.
 * Generates a transactionId for every incoming request, logs the entry point
 * and the final HTTP status. Response bodies are never logged — token endpoint
 * responses contain JWT access tokens that must not appear in logs.
 */
@Component
@Order(1)
public class TransactionIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TransactionIdFilter.class);
    private static final String TRANSACTION_ID = "transactionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String txId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID, txId);
        response.setHeader("X-Transaction-Id", txId);

        log.info(">>> {} {} [client={}]",
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("<<< {} {} -> HTTP {}",
                    request.getMethod(), request.getRequestURI(), response.getStatus());
            MDC.remove(TRANSACTION_ID);
        }
    }
}
