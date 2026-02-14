package com.epam.gym.core.dao.impl;

import com.epam.gym.core.dao.TrainingTypeDao;
import com.epam.gym.core.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeDaoImpl.class);

    private final Map<Long, TrainingType> storage;
    private long currentId = 1;

    @Autowired
    public TrainingTypeDaoImpl(@Qualifier("trainingTypeStorage") Map<Long, TrainingType> storage) {
        this.storage = storage;
    }

    @Override
    public TrainingType create(TrainingType trainingType) {
        log.debug("Creating training type: {}", trainingType);

        Long id = currentId++;
        trainingType.setId(id);
        storage.put(id, trainingType);

        log.info("Created training type with id: {}", id);
        return trainingType;
    }

    @Override
    public Optional<TrainingType> findById(Long id) {
        log.debug("Finding training type by id: {}", id);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<TrainingType> findByName(String name) {
        log.debug("Finding training type by name: {}", name);

        return storage.values().stream()
                .filter(trainingType -> name.equals(trainingType.getTrainingTypeName()))
                .findFirst();
    }

    @Override
    public List<TrainingType> findAll() {
        log.debug("Finding all training types");
        return new ArrayList<>(storage.values());
    }
}
