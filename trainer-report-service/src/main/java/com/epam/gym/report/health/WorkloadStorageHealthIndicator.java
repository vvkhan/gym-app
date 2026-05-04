package com.epam.gym.report.health;

import com.epam.gym.report.repository.TrainerSummaryRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class WorkloadStorageHealthIndicator implements HealthIndicator {

    private final TrainerSummaryRepository repository;

    public WorkloadStorageHealthIndicator(TrainerSummaryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Health health() {
        try {
            long trackedTrainers = repository.count();
            return Health.up()
                    .withDetail("trackedTrainers", trackedTrainers)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("reason", "Workload storage unavailable: " + e.getMessage())
                    .build();
        }
    }
}
