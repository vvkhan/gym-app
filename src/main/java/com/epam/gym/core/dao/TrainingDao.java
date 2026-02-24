package com.epam.gym.core.dao;

import com.epam.gym.core.model.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingDao {

    Training create(Training training);

    Optional<Training> findById(Long id);

    List<Training> findAll();

    List<Training> findByTraineeId(Long traineeId);

    List<Training> findByTrainerId(Long trainerId);
}
