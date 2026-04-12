package com.epam.gym.report.health;

import com.epam.gym.report.repository.TrainerWorkloadRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

@Component
public class ReportMetrics implements MeterBinder {

    private final TrainerWorkloadRepository repository;

    public ReportMetrics(TrainerWorkloadRepository repository) {
        this.repository = repository;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("report.trainers.tracked", repository, TrainerWorkloadRepository::count)
                .description("Total number of trainers with recorded workload")
                .register(registry);
    }
}
