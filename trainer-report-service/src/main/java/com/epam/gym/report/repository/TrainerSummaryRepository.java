package com.epam.gym.report.repository;

import com.epam.gym.report.model.TrainerSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerSummaryRepository extends MongoRepository<TrainerSummary, String> {

    Optional<TrainerSummary> findByUsername(String username);

    List<TrainerSummary> findByLastNameAndFirstName(String lastName, String firstName);
}
