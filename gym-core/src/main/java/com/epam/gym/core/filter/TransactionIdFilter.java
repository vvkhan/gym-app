package com.epam.gym.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class TransactionIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TransactionIdFilter.class);
    private static final String TRANSACTION_ID = "transactionId";
    private static final int MAX_BODY_LOG_LENGTH = 500;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String transactionId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID, transactionId);

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        wrappedResponse.setHeader("X-Transaction-Id", transactionId);

        // Level 1 logging — transaction entry point
        log.info(">>> {} {} [client={}]",
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        try {
            filterChain.doFilter(request, wrappedResponse);
        } finally {
            int status = wrappedResponse.getStatus();
            if (status >= 400) {
                // Log response body only for errors — avoids exposing tokens from 2xx responses
                String body = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
                String truncated = body.length() > MAX_BODY_LOG_LENGTH
                        ? body.substring(0, MAX_BODY_LOG_LENGTH) + "...[truncated]"
                        : body;
                log.info("<<< {} {} -> HTTP {} | body: {}",
                        request.getMethod(), request.getRequestURI(), status, truncated);
            } else {
                log.info("<<< {} {} -> HTTP {}",
                        request.getMethod(), request.getRequestURI(), status);
            }
            wrappedResponse.copyBodyToResponse();
            MDC.remove(TRANSACTION_ID);
        }
    }
}
