package com.epam.gym.core.repository;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class TrainerSpecification {

    private TrainerSpecification() {}

    public static Specification<Trainer> notAssignedToTrainee(String traineeUsername) {
        return (root, query, cb) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<Trainee> traineeRoot = subquery.from(Trainee.class);
            Join<Trainee, Trainer> trainerJoin = traineeRoot.join("trainers");
            subquery.select(trainerJoin.get("id"))
                    .where(cb.equal(traineeRoot.get("user").get("username"), traineeUsername));
            return cb.not(root.get("id").in(subquery));
        };
    }
}
