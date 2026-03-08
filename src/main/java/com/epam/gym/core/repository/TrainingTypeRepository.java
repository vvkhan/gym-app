package com.epam.gym.core.repository;

import com.epam.gym.core.model.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrainingTypeRepository extends JpaRepository<TrainingType, UUID> {

    Optional<TrainingType> findByTrainingTypeName(String name);
}
