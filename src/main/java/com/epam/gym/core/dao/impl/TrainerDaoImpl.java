package com.epam.gym.core.dao.impl;

import com.epam.gym.core.dao.TrainerDao;
import java.util.NoSuchElementException;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.aspect.LogExecution;
import com.epam.gym.core.model.TrainingType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@LogExecution
@Repository
@DependsOn("trainerDataLoader")
public class TrainerDaoImpl implements TrainerDao {

    private Map<Long, Trainer> storage;
    private long currentId = 1;

    @Autowired
    public void setStorage(@Qualifier("trainerStorage") Map<Long, Trainer> storage) {
        this.storage = storage;
    }

    @PostConstruct
    public void initCurrentId() {
        if (!storage.isEmpty()) {
            currentId = Collections.max(storage.keySet()) + 1;
        }
    }

    @Override
    public Trainer create(Trainer trainer) {
        Long id = currentId++;
        Trainer stored = Trainer.builder()
                .id(id)
                .firstName(trainer.getFirstName())
                .lastName(trainer.getLastName())
                .username(trainer.getUsername())
                .password(trainer.getPassword())
                .isActive(trainer.getIsActive())
                .specialization(copyTrainingType(trainer.getSpecialization()))
                .build();
        storage.put(id, stored);
        return copy(stored);
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        return Optional.ofNullable(storage.get(id)).map(this::copy);
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        return storage.values().stream()
                .filter(trainer -> username.equals(trainer.getUsername()))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public List<Trainer> findAll() {
        return storage.values().stream()
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public Trainer update(Trainer trainer) {
        if (trainer.getId() == null || !storage.containsKey(trainer.getId())) {
            throw new NoSuchElementException("Trainer with id " + trainer.getId() + " not found");
        }
        Trainer stored = copy(trainer);
        storage.put(stored.getId(), stored);
        return copy(stored);
    }

    private Trainer copy(Trainer t) {
        return t.toBuilder()
                .specialization(copyTrainingType(t.getSpecialization()))
                .build();
    }

    private TrainingType copyTrainingType(TrainingType t) {
        if (t == null) return null;
        return new TrainingType(t.getId(), t.getTrainingTypeName());
    }
}
