package com.epam.gym.core.util;

import com.epam.gym.core.aspect.LogExecution;
import com.epam.gym.core.dao.TraineeDao;
import com.epam.gym.core.dao.TrainerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@LogExecution
@Component
public class UsernameGeneratorImpl implements UsernameGenerator {

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;

    @Autowired
    public UsernameGeneratorImpl(TraineeDao traineeDao, TrainerDao trainerDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
    }

    @Override
    public String generateUsername(String firstName, String lastName) {
        String base = firstName + "." + lastName;

        // 2 queries total — fetch all taken usernames with this prefix upfront
        Set<String> taken = new HashSet<>();
        taken.addAll(traineeDao.findUsernamesByPrefix(base));
        taken.addAll(trainerDao.findUsernamesByPrefix(base));

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
