package com.epam.gym.report.service;

import com.epam.gym.report.aspect.LogExecution;
import com.epam.gym.report.dto.ActionType;
import com.epam.gym.report.dto.WorkloadRequest;
import com.epam.gym.report.dto.WorkloadSummaryResponse;
import com.epam.gym.report.mapper.WorkloadMapper;
import com.epam.gym.report.model.TrainerSummary;
import com.epam.gym.report.model.TrainerSummary.MonthSummary;
import com.epam.gym.report.model.TrainerSummary.YearSummary;
import com.epam.gym.report.repository.TrainerSummaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@LogExecution
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final TrainerSummaryRepository repository;
    private final WorkloadMapper workloadMapper;

    public ReportService(TrainerSummaryRepository repository, WorkloadMapper workloadMapper) {
        this.repository = repository;
        this.workloadMapper = workloadMapper;
    }

    // Transaction-level log: one INFO per business operation
    public void processWorkload(WorkloadRequest request) {
        String txId = MDC.get("transactionId");
        log.info("[txId={}] processWorkload START action={} trainer={}",
                txId, request.getActionType(), request.getTrainerUsername());

        TrainerSummary summary = repository.findByUsername(request.getTrainerUsername())
                .orElseGet(() -> createNewSummary(request, txId));

        summary.setFirstName(request.getFirstName());
        summary.setLastName(request.getLastName());
        summary.setActive(request.isActive());

        int year  = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        YearSummary yearSummary = findOrCreateYear(summary, year, txId);
        MonthSummary monthSummary = findOrCreateMonth(yearSummary, month, txId);

        // Operation-level log: duration delta before persistence
        int prev  = monthSummary.getTotalDurationMinutes();
        int delta = request.getActionType() == ActionType.ADD
                ? request.getTrainingDurationMinutes()
                : -request.getTrainingDurationMinutes();
        int updated = Math.max(0, prev + delta);
        log.debug("[txId={}] duration update: year={} month={} prev={} delta={} next={}",
                txId, year, month, prev, delta, updated);

        monthSummary.setTotalDurationMinutes(updated);
        repository.save(summary);

        log.info("[txId={}] processWorkload END trainer={} year={} month={} duration={}",
                txId, request.getTrainerUsername(), year, month, updated);
    }

    // Transaction-level log: one INFO per read operation
    public WorkloadSummaryResponse getSummary(String username) {
        log.info("[txId={}] getSummary username={}", MDC.get("transactionId"), username);
        TrainerSummary summary = repository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("No workload data for trainer: " + username));
        return workloadMapper.toResponse(summary);
    }

    // Helpers

    private TrainerSummary createNewSummary(WorkloadRequest request, String txId) {
        log.debug("[txId={}] No existing record for trainer={}, creating new document", txId, request.getTrainerUsername());
        TrainerSummary s = new TrainerSummary();
        s.setUsername(request.getTrainerUsername());
        s.setFirstName(request.getFirstName());
        s.setLastName(request.getLastName());
        s.setActive(request.isActive());
        return s;
    }

    private YearSummary findOrCreateYear(TrainerSummary summary, int year, String txId) {
        return summary.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("[txId={}] Creating year entry year={}", txId, year);
                    YearSummary y = new YearSummary(year);
                    summary.getYears().add(y);
                    return y;
                });
    }

    private MonthSummary findOrCreateMonth(YearSummary yearSummary, int month, String txId) {
        return yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("[txId={}] Creating month entry month={}", txId, month);
                    MonthSummary m = new MonthSummary(month);
                    yearSummary.getMonths().add(m);
                    return m;
                });
    }
}
