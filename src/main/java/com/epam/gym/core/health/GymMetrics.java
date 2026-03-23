package com.epam.gym.core.health;

import com.epam.gym.core.repository.TrainingRepository;
import com.epam.gym.core.repository.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics implements MeterBinder {

    private final UserRepository userRepository;
    private final TrainingRepository trainingRepository;

    public GymMetrics(UserRepository userRepository, TrainingRepository trainingRepository) {
        this.userRepository = userRepository;
        this.trainingRepository = trainingRepository;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("gym.users.registered", userRepository, UserRepository::count)
                .description("Total number of registered users")
                .register(registry);

        Gauge.builder("gym.trainings.total", trainingRepository, TrainingRepository::count)
                .description("Total number of trainings")
                .register(registry);
    }
}
