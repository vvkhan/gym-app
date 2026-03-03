package com.epam.gym.core.repository;

import com.epam.gym.core.model.Training;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainingRepository extends JpaRepository<Training, UUID>,
                                             JpaSpecificationExecutor<Training> {

    @Override
    @EntityGraph(attributePaths = {"trainee.user", "trainer.user", "trainer.specialization", "trainingType"})
    Optional<Training> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = {"trainee.user", "trainer.user", "trainer.specialization", "trainingType"})
    List<Training> findAll();

    @Override
    @EntityGraph(attributePaths = {"trainee.user", "trainer.user", "trainer.specialization", "trainingType"})
    List<Training> findAll(Specification<Training> spec);

    @EntityGraph(attributePaths = {"trainee.user", "trainer.user", "trainer.specialization", "trainingType"})
    List<Training> findByTraineeId(UUID traineeId);

    @EntityGraph(attributePaths = {"trainee.user", "trainer.user", "trainer.specialization", "trainingType"})
    List<Training> findByTrainerId(UUID trainerId);
}
