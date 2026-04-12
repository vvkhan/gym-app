package com.epam.gym.report.repository;

import com.epam.gym.report.model.TrainerWorkload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkload, UUID> {

    Optional<TrainerWorkload> findByUsername(String username);
}
