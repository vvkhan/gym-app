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

    @EntityGraph(Trainee.FULL_GRAPH)
    Optional<Trainee> findByUserUsername(String username);

    @Override
    @EntityGraph(Trainee.FULL_GRAPH)
    Optional<Trainee> findById(UUID id);

    @Override
    @EntityGraph(Trainee.FULL_GRAPH)
    List<Trainee> findAll();

    boolean existsByUserId(UUID userId);

    boolean existsByUserUsernameAndUserPassword(String username, String password);

    void deleteByUserUsername(String username);

    @Query("select t.user.username from Trainee t " +
           "where t.user.username = :exact or t.user.username like :pattern")
    List<String> findUsernamesByPrefix(@Param("exact") String exact, @Param("pattern") String pattern);
}
