package com.epam.gym.core.health;

import com.epam.gym.core.repository.TrainingTypeRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TrainingTypesHealthIndicator implements HealthIndicator {

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypesHealthIndicator(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public Health health() {
        long count = trainingTypeRepository.count();
        if (count > 0) {
            return Health.up()
                    .withDetail("trainingTypes", count)
                    .build();
        }
        return Health.down()
                .withDetail("reason", "No training types found in the database")
                .build();
    }
}
