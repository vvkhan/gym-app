package com.epam.gym.report.controller;

import com.epam.gym.report.dto.WorkloadRequest;
import com.epam.gym.report.dto.WorkloadSummaryResponse;
import com.epam.gym.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
@Tag(name = "Trainer Report", description = "Trainer workload reporting")
public class TrainerReportController {

    private final ReportService reportService;

    public TrainerReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Called by gym-core every time training is added or deleted
    // Requires scope: report:write
    @PostMapping
    @Operation(summary = "Update trainer workload")
    @ApiResponse(responseCode = "200", description = "Workload updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token")
    public ResponseEntity<Void> updateWorkload(@Valid @RequestBody WorkloadRequest request) {
        reportService.processWorkload(request);
        return ResponseEntity.ok().build();
    }

    // Returns full workload summary for trainer (all years and months)
    // Requires scope: report:read
    @GetMapping("/{username}")
    @Operation(summary = "Get full trainer workload summary")
    @ApiResponse(responseCode = "200", description = "Summary returned")
    @ApiResponse(responseCode = "404", description = "No data found for trainer")
    public ResponseEntity<WorkloadSummaryResponse> getSummary(@PathVariable String username) {
        return ResponseEntity.ok(reportService.getSummary(username));
    }

    // Returns total training minutes for a trainer in a specific month
    // Returns 0 if no trainings have been recorded for that period
    // Requires scope: report:read
    @GetMapping("/{username}/{year}/{month}")
    @Operation(summary = "Get trainer workload for a specific month")
    @ApiResponse(responseCode = "200", description = "Duration in minutes returned")
    public ResponseEntity<Integer> getMonthlyDuration(@PathVariable String username,
                                                      @PathVariable int year,
                                                      @PathVariable int month) {
        return ResponseEntity.ok(reportService.getMonthlyDuration(username, year, month));
    }
}
