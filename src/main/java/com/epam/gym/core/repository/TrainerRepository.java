package com.epam.gym.core.repository;

import com.epam.gym.core.model.Trainer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainerRepository extends JpaRepository<Trainer, UUID>,
                                           JpaSpecificationExecutor<Trainer> {

    @EntityGraph(attributePaths = {"user", "specialization"})
    Optional<Trainer> findByUserUsername(String username);

    @Override
    @EntityGraph(attributePaths = {"user", "specialization"})
    Optional<Trainer> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = {"user", "specialization"})
    List<Trainer> findAll();

    @Override
    @EntityGraph(attributePaths = {"user", "specialization"})
    List<Trainer> findAll(Specification<Trainer> spec);

    @EntityGraph(attributePaths = {"user", "specialization"})
    List<Trainer> findByUserUsernameIn(Collection<String> usernames);

    boolean existsByUserId(UUID userId);

    @Query("select t.user.username from Trainer t " +
           "where t.user.username = :exact or t.user.username like :pattern")
    List<String> findUsernamesByPrefix(@Param("exact") String exact, @Param("pattern") String pattern);
}
