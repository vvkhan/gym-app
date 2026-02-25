package com.epam.gym.core.dao;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Training;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface TraineeDao {

    Trainee create(Trainee trainee);

    Optional<Trainee> findById(Long id);

    Optional<Trainee> findByUsername(String username);

    List<Trainee> findAll();

    Trainee update(Trainee trainee);

    void delete(String username);

    List<Training> findTrainingsByCriteria(String traineeUsername, LocalDate fromDate,
                                           LocalDate toDate, String trainerName,
                                           String trainingTypeName);

    List<String> findUsernamesByPrefix(String prefix);
}
