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
    @EntityGraph(Training.FULL_GRAPH)
    Optional<Training> findById(UUID id);

    @Override
    @EntityGraph(Training.FULL_GRAPH)
    List<Training> findAll();

    @Override
    @EntityGraph(Training.FULL_GRAPH)
    List<Training> findAll(Specification<Training> spec);

    @EntityGraph(Training.FULL_GRAPH)
    List<Training> findByTraineeId(UUID traineeId);

    @EntityGraph(Training.FULL_GRAPH)
    List<Training> findByTrainerId(UUID trainerId);
}
