package com.epam.gym.core.dao;

import com.epam.gym.core.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {

    Trainee create(Trainee trainee);

    Optional<Trainee> findById(Long id);

    Optional<Trainee> findByUsername(String username);

    List<Trainee> findAll();

    Trainee update(Trainee trainee);

    void delete(Long id);
}
