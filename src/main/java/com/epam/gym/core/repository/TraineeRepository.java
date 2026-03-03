package com.epam.gym.core.repository;

import com.epam.gym.core.model.Trainee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TraineeRepository extends JpaRepository<Trainee, UUID> {

    @EntityGraph(attributePaths = {"user", "trainers.user", "trainers.specialization"})
    Optional<Trainee> findByUserUsername(String username);

    @Override
    @EntityGraph(attributePaths = {"user", "trainers.user", "trainers.specialization"})
    Optional<Trainee> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = {"user", "trainers.user", "trainers.specialization"})
    List<Trainee> findAll();

    boolean existsByUserId(UUID userId);

    void deleteByUserUsername(String username);

    @Query("select t.user.username from Trainee t " +
           "where t.user.username = :exact or t.user.username like :pattern")
    List<String> findUsernamesByPrefix(@Param("exact") String exact, @Param("pattern") String pattern);
}
