package com.epam.gym.core.dao.impl;

import com.epam.gym.core.dao.TrainingDao;
import com.epam.gym.core.model.Training;
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

@Repository
@DependsOn("trainingDataLoader")
public class TrainingDaoImpl implements TrainingDao {

    private Map<Long, Training> storage;
    private long currentId = 1;

    @Autowired
    public void setStorage(@Qualifier("trainingStorage") Map<Long, Training> storage) {
        this.storage = storage;
    }

    @PostConstruct
    public void initCurrentId() {
        if (!storage.isEmpty()) {
            currentId = Collections.max(storage.keySet()) + 1;
        }
    }

    @Override
    public Training create(Training training) {
        Long id = currentId++;
        Training stored = Training.builder()
                .id(id)
                .traineeId(training.getTraineeId())
                .trainerId(training.getTrainerId())
                .trainingName(training.getTrainingName())
                .trainingType(copyTrainingType(training.getTrainingType()))
                .trainingDate(training.getTrainingDate())
                .duration(training.getDuration())
                .build();
        storage.put(id, stored);
        return copy(stored);
    }

    @Override
    public Optional<Training> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        return Optional.ofNullable(storage.get(id)).map(this::copy);
    }

    @Override
    public List<Training> findAll() {
        return storage.values().stream()
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<Training> findByTraineeId(Long traineeId) {
        if (traineeId == null) {
            throw new IllegalArgumentException("Trainee id must not be null");
        }
        return storage.values().stream()
                .filter(training -> traineeId.equals(training.getTraineeId()))
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<Training> findByTrainerId(Long trainerId) {
        if (trainerId == null) {
            throw new IllegalArgumentException("Trainer id must not be null");
        }
        return storage.values().stream()
                .filter(training -> trainerId.equals(training.getTrainerId()))
                .map(this::copy)
                .collect(Collectors.toList());
    }

    private Training copy(Training t) {
        return Training.builder()
                .id(t.getId())
                .traineeId(t.getTraineeId())
                .trainerId(t.getTrainerId())
                .trainingName(t.getTrainingName())
                .trainingType(copyTrainingType(t.getTrainingType()))
                .trainingDate(t.getTrainingDate())
                .duration(t.getDuration())
                .build();
    }

    private TrainingType copyTrainingType(TrainingType t) {
        if (t == null) return null;
        return new TrainingType(t.getId(), t.getTrainingTypeName());
    }
}
