package com.epam.gym.report.service;

import com.epam.gym.report.aspect.LogExecution;
import com.epam.gym.report.dto.ActionType;
import com.epam.gym.report.dto.WorkloadRequest;
import com.epam.gym.report.dto.WorkloadSummaryResponse;
import com.epam.gym.report.mapper.WorkloadMapper;
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
    private final WorkloadMapper workloadMapper;

    public ReportService(TrainerWorkloadRepository repository, WorkloadMapper workloadMapper) {
        this.repository = repository;
        this.workloadMapper = workloadMapper;
    }

    /**
     * Processes ADD/DELETE training events from gym-core.
     * For ADD: adds the duration to the trainer's monthly total.
     * For DELETE: subtracts it (floor at 0 to guard against out-of-order events).
     */
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
        return workloadMapper.toResponse(workload);
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

}
