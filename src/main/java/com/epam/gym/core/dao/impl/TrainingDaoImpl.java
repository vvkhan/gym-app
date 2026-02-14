package com.epam.gym.core.dao.impl;

import com.epam.gym.core.dao.TrainingDao;
import com.epam.gym.core.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class TrainingDaoImpl implements TrainingDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingDaoImpl.class);

    private final Map<Long, Training> storage;
    private long currentId = 1;

    @Autowired
    public TrainingDaoImpl(@Qualifier("trainingStorage") Map<Long, Training> storage) {
        this.storage = storage;
    }

    @Override
    public Training create(Training training) {
        log.debug("Creating training: {}", training);

        Long id = currentId++;
        training.setId(id);
        storage.put(id, training);

        log.info("Created training with id: {}", id);
        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        log.debug("Finding training by id: {}", id);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Training> findAll() {
        log.debug("Finding all trainings");
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Training> findByTraineeId(Long traineeId) {
        log.debug("Finding trainings by trainee id: {}", traineeId);

        return storage.values().stream()
                .filter(training -> traineeId.equals(training.getTraineeId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Training> findByTrainerId(Long trainerId) {
        log.debug("Finding trainings by trainer id: {}", trainerId);

        return storage.values().stream()
                .filter(training -> trainerId.equals(training.getTrainerId()))
                .collect(Collectors.toList());
    }
}
