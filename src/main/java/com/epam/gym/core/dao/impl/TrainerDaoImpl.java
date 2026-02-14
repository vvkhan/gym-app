package com.epam.gym.core.dao.impl;

import com.epam.gym.core.dao.TrainerDao;
import com.epam.gym.core.exception.EntityNotFoundException;
import com.epam.gym.core.model.Trainer;
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
public class TrainerDaoImpl implements TrainerDao {

    private static final Logger log = LoggerFactory.getLogger(TrainerDaoImpl.class);

    private final Map<Long, Trainer> storage;
    private long currentId = 1;

    @Autowired
    public TrainerDaoImpl(@Qualifier("trainerStorage") Map<Long, Trainer> storage) {
        this.storage = storage;
    }

    @Override
    public Trainer create(Trainer trainer) {
        log.debug("Creating trainer: {}", trainer);

        Long id = currentId++;
        trainer.setId(id);
        storage.put(id, trainer);

        log.info("Created trainer with id: {}", id);
        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        log.debug("Finding trainer by id: {}", id);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        log.debug("Finding trainer by username: {}", username);

        return storage.values().stream()
                .filter(trainer -> username.equals(trainer.getUsername()))
                .findFirst();
    }

    @Override
    public List<Trainer> findAll() {
        log.debug("Finding all trainers");
        return new ArrayList<>(storage.values());
    }

    @Override
    public Trainer update(Trainer trainer) {
        log.debug("Updating trainer: {}", trainer);

        if (trainer.getId() == null || !storage.containsKey(trainer.getId())) {
            log.error("Trainer not found for update: {}", trainer.getId());
            throw new EntityNotFoundException("Trainer", trainer.getId());
        }

        storage.put(trainer.getId(), trainer);
        log.info("Updated trainer with id: {}", trainer.getId());
        return trainer;
    }
}
