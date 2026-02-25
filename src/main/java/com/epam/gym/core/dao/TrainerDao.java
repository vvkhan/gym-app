package com.epam.gym.core.dao;

import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface TrainerDao {

    Trainer create(Trainer trainer);

    Optional<Trainer> findById(Long id);

    Optional<Trainer> findByUsername(String username);

    List<Trainer> findAll();

    Trainer update(Trainer trainer);

    List<Trainer> findNotAssignedTrainers(String traineeUsername);

    List<Training> findTrainingsByCriteria(String trainerUsername, LocalDate fromDate,
                                           LocalDate toDate, String traineeName);

    List<String> findUsernamesByPrefix(String prefix);
}
