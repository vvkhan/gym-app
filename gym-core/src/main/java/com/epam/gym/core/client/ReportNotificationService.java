package com.epam.gym.core.client;

import com.epam.gym.core.dto.request.WorkloadRequest;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Wraps the Feign call to trainer-report-service with Resilience4j @Retry
 */
@Service
public class ReportNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ReportNotificationService.class);

    private final ReportServiceClient reportServiceClient;

    public ReportNotificationService(ReportServiceClient reportServiceClient) {
        this.reportServiceClient = reportServiceClient;
    }

    @Retry(name = "trainer-report-service", fallbackMethod = "notifyFallback")
    public void notify(String txId, WorkloadRequest request) {
        reportServiceClient.updateWorkload(txId, request);
    }

    private void notifyFallback(String txId, WorkloadRequest request, Exception e) {
        log.error("[txId={}] All retries exhausted or circuit OPEN for trainer-report-service. " +
                  "Dropping workload event: trainer={}, action={}. Cause: {}",
                txId, request.getTrainerUsername(), request.getActionType(), e.getMessage());
    }
}
