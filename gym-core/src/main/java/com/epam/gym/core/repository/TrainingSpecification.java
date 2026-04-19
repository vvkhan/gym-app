package com.epam.gym.core.repository;

import com.epam.gym.core.model.Training;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TrainingSpecification {

    private TrainingSpecification() {}

    public static Specification<Training> byTraineeCriteria(
            String traineeUsername, LocalDate fromDate, LocalDate toDate,
            String trainerUsername, String trainingTypeName) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(
                    root.get("trainee").get("user").get("username"), traineeUsername));
            if (fromDate != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
            if (toDate != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), toDate));
            if (trainerUsername != null)
                predicates.add(cb.equal(
                        root.get("trainer").get("user").get("username"), trainerUsername));
            if (trainingTypeName != null)
                predicates.add(cb.equal(
                        root.get("trainingType").get("trainingTypeName"), trainingTypeName));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Training> byTrainerCriteria(
            String trainerUsername, LocalDate fromDate, LocalDate toDate, String traineeUsername) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(
                    root.get("trainer").get("user").get("username"), trainerUsername));
            if (fromDate != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
            if (toDate != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), toDate));
            if (traineeUsername != null)
                predicates.add(cb.equal(
                        root.get("trainee").get("user").get("username"), traineeUsername));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
