package com.epam.gym.core.dao;

import com.epam.gym.core.model.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeDao {

    TrainingType create(TrainingType trainingType);

    Optional<TrainingType> findById(Long id);

    Optional<TrainingType> findByName(String name);

    List<TrainingType> findAll();
}
