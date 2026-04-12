package com.epam.gym.report.filter;

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
 * Reads the X-Transaction-Id header forwarded by gym-core and places it in MDC.
 * If header is missing (in case of direct calls in testing), fallback ID is generated.
 */
@Component
@Order(1)
public class TransactionIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TransactionIdFilter.class);
    private static final String TRANSACTION_ID = "transactionId";
    private static final String HEADER = "X-Transaction-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String txId = request.getHeader(HEADER);
        if (txId == null || txId.isBlank()) {
            txId = "local-" + UUID.randomUUID();
        }

        MDC.put(TRANSACTION_ID, txId);
        response.setHeader(HEADER, txId);

        log.info(">>> {} {}", request.getMethod(), request.getRequestURI());
        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("<<< {} {} -> HTTP {}", request.getMethod(), request.getRequestURI(), response.getStatus());
            MDC.remove(TRANSACTION_ID);
        }
    }
}
