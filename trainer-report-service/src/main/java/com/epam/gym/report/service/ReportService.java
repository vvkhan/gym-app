package com.epam.gym.report.service;

import com.epam.gym.report.aspect.LogExecution;
import com.epam.gym.report.dto.ActionType;
import com.epam.gym.report.dto.WorkloadRequest;
import com.epam.gym.report.dto.WorkloadSummaryResponse;
import com.epam.gym.report.model.TrainerWorkload;
import com.epam.gym.report.model.TrainingMonth;
import com.epam.gym.report.model.TrainingYear;
import com.epam.gym.report.repository.TrainerWorkloadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@LogExecution
@Service
public class ReportService {

    private final TrainerWorkloadRepository repository;

    public ReportService(TrainerWorkloadRepository repository) {
        this.repository = repository;
    }

    // Processing of ADD/DELETE training events from gym-core:
    // For ADD: adds the duration to the trainer's monthly total
    // For DELETE: subtracts it (floor at 0 to guard against out-of-order events)
    @Transactional
    public void processWorkload(WorkloadRequest request) {
        TrainerWorkload workload = repository
                .findByUsername(request.getTrainerUsername())
                .orElseGet(() -> buildNewWorkload(request));

        // Sync name and status if they updated in gym-core
        workload.setFirstName(request.getFirstName());
        workload.setLastName(request.getLastName());
        workload.setActive(request.isActive());

        int year  = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        TrainingYear yearEntry = workload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> addYear(workload, year));

        TrainingMonth monthEntry = yearEntry.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> addMonth(yearEntry, month));

        if (request.getActionType() == ActionType.ADD) {
            monthEntry.setTotalDurationMinutes(
                    monthEntry.getTotalDurationMinutes() + request.getTrainingDurationMinutes());
        } else {
            int updated = monthEntry.getTotalDurationMinutes() - request.getTrainingDurationMinutes();
            monthEntry.setTotalDurationMinutes(Math.max(0, updated));
        }

        repository.save(workload);
    }

    // Returns full workload summary for trainer
    @Transactional(readOnly = true)
    public WorkloadSummaryResponse getSummary(String username) {
        TrainerWorkload workload = repository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("No workload data for trainer: " + username));
        return toResponse(workload);
    }

    // Returns total training duration (in minutes) for trainer in specific month
    @Transactional(readOnly = true)
    public int getMonthlyDuration(String username, int year, int month) {
        return repository.findByUsername(username)
                .flatMap(w -> w.getYears().stream()
                        .filter(y -> y.getYear() == year)
                        .findFirst())
                .flatMap(y -> y.getMonths().stream()
                        .filter(m -> m.getMonth() == month)
                        .findFirst())
                .map(TrainingMonth::getTotalDurationMinutes)
                .orElse(0);
    }

    // Helpers

    private TrainerWorkload buildNewWorkload(WorkloadRequest request) {
        TrainerWorkload w = new TrainerWorkload();
        w.setUsername(request.getTrainerUsername());
        w.setFirstName(request.getFirstName());
        w.setLastName(request.getLastName());
        w.setActive(request.isActive());
        return w;
    }

    private TrainingYear addYear(TrainerWorkload workload, int year) {
        TrainingYear y = new TrainingYear();
        y.setYear(year);
        y.setWorkload(workload);
        workload.getYears().add(y);
        return y;
    }

    private TrainingMonth addMonth(TrainingYear yearEntry, int month) {
        TrainingMonth m = new TrainingMonth();
        m.setMonth(month);
        m.setTotalDurationMinutes(0);
        m.setTrainingYear(yearEntry);
        yearEntry.getMonths().add(m);
        return m;
    }

    private WorkloadSummaryResponse toResponse(TrainerWorkload workload) {
        WorkloadSummaryResponse response = new WorkloadSummaryResponse();
        response.setUsername(workload.getUsername());
        response.setFirstName(workload.getFirstName());
        response.setLastName(workload.getLastName());
        response.setActive(workload.isActive());
        response.setYears(workload.getYears().stream()
                .map(y -> {
                    WorkloadSummaryResponse.YearSummary yearSummary = new WorkloadSummaryResponse.YearSummary();
                    yearSummary.setYear(y.getYear());
                    yearSummary.setMonths(y.getMonths().stream()
                            .map(m -> {
                                WorkloadSummaryResponse.MonthSummary monthSummary = new WorkloadSummaryResponse.MonthSummary();
                                monthSummary.setMonth(m.getMonth());
                                monthSummary.setTotalDurationMinutes(m.getTotalDurationMinutes());
                                return monthSummary;
                            })
                            .toList());
                    return yearSummary;
                })
                .toList());
        return response;
    }
}
