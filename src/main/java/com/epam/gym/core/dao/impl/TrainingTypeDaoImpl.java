package com.epam.gym.core.dao.impl;

import com.epam.gym.core.dao.TrainingTypeDao;
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
@DependsOn("trainingTypeDataLoader")
public class TrainingTypeDaoImpl implements TrainingTypeDao {

    private Map<Long, TrainingType> storage;
    private long currentId = 1;

    @Autowired
    public void setStorage(@Qualifier("trainingTypeStorage") Map<Long, TrainingType> storage) {
        this.storage = storage;
    }

    @PostConstruct
    public void initCurrentId() {
        if (!storage.isEmpty()) {
            currentId = Collections.max(storage.keySet()) + 1;
        }
    }

    @Override
    public TrainingType create(TrainingType trainingType) {
        Long id = currentId++;
        TrainingType stored = new TrainingType(id, trainingType.getTrainingTypeName());
        storage.put(id, stored);
        return copy(stored);
    }

    @Override
    public Optional<TrainingType> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        return Optional.ofNullable(storage.get(id)).map(this::copy);
    }

    @Override
    public Optional<TrainingType> findByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        return storage.values().stream()
                .filter(trainingType -> name.equals(trainingType.getTrainingTypeName()))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public List<TrainingType> findAll() {
        return storage.values().stream()
                .map(this::copy)
                .collect(Collectors.toList());
    }

    private TrainingType copy(TrainingType t) {
        return new TrainingType(t.getId(), t.getTrainingTypeName());
    }
}
