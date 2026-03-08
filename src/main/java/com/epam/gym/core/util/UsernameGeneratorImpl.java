package com.epam.gym.core.util;

import com.epam.gym.core.aspect.LogExecution;
import com.epam.gym.core.repository.TraineeRepository;
import com.epam.gym.core.repository.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@LogExecution
@Component
public class UsernameGeneratorImpl implements UsernameGenerator {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    @Autowired
    public UsernameGeneratorImpl(TraineeRepository traineeRepository, TrainerRepository trainerRepository) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
    }

    @Override
    public String generateUsername(String firstName, String lastName) {
        String base = firstName + "." + lastName;

        Set<String> taken = new HashSet<>();
        taken.addAll(traineeRepository.findUsernamesByPrefix(base, base + "%"));
        taken.addAll(trainerRepository.findUsernamesByPrefix(base, base + "%"));

        if (!taken.contains(base)) {
            return base;
        }
        for (int counter = 1; ; counter++) {
            String candidate = base + counter;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }
}
