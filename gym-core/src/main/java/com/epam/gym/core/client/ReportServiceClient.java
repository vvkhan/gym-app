package com.epam.gym.core.client;

import com.epam.gym.core.dto.request.WorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for trainer-report-service.
 */
@FeignClient(name = "trainer-report-service")
public interface ReportServiceClient {

    @PostMapping("/api/report")
    ResponseEntity<Void> updateWorkload(
            @RequestHeader("X-Transaction-Id") String transactionId,
            @RequestBody WorkloadRequest request);
}
