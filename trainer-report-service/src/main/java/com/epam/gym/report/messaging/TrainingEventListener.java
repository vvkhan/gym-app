package com.epam.gym.report.messaging;

import com.epam.gym.report.dto.WorkloadRequest;
import com.epam.gym.report.service.ReportService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TrainingEventListener {

    private static final Logger log = LoggerFactory.getLogger(TrainingEventListener.class);

    private final ReportService reportService;
    private final Validator validator;

    public TrainingEventListener(ReportService reportService, Validator validator) {
        this.reportService = reportService;
        this.validator = validator;
    }

    /**
     * Consumes training workload events published by gym-core.
     * Throws on validation failure so ActiveMQ retries and eventually routes to DLQ automatically.
     */
    @JmsListener(destination = "${app.messaging.training-events-queue}")
    public void onTrainingEvent(WorkloadRequest request,
                                @Header(name = "transactionId", required = false) String txId) {
        String resolvedTxId = txId != null ? txId : "no-tx";
        MDC.put("transactionId", resolvedTxId);
        try {
            Set<ConstraintViolation<WorkloadRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                String reasons = violations.stream()
                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                        .collect(Collectors.joining(", "));
                log.error("[txId={}] Invalid message, will be routed to DLQ by broker — violations: {}", resolvedTxId, reasons);
                throw new IllegalArgumentException("Invalid WorkloadRequest: " + reasons);
            }

            log.debug("[txId={}] Received {} event for trainer={}", resolvedTxId,
                    request.getActionType(), request.getTrainerUsername());
            reportService.processWorkload(request);
        } finally {
            MDC.remove("transactionId");
        }
    }
}
