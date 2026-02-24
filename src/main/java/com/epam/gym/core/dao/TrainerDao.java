package com.epam.gym.core.dao;

import com.epam.gym.core.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {

    Trainer create(Trainer trainer);

    Optional<Trainer> findById(Long id);

    Optional<Trainer> findByUsername(String username);

    List<Trainer> findAll();

    Trainer update(Trainer trainer);
}
