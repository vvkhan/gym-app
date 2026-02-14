package com.epam.gym.core.dao.impl;

import com.epam.gym.core.dao.TraineeDao;
import com.epam.gym.core.exception.EntityNotFoundException;
import com.epam.gym.core.model.Trainee;
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
public class TraineeDaoImpl implements TraineeDao {

    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);

    private final Map<Long, Trainee> storage;
    private long currentId = 1;

    @Autowired
    public TraineeDaoImpl(@Qualifier("traineeStorage") Map<Long, Trainee> storage) {
        this.storage = storage;
    }

    @Override
    public Trainee create(Trainee trainee) {
        log.debug("Creating trainee: {}", trainee);

        Long id = currentId++;
        trainee.setId(id);
        storage.put(id, trainee);

        log.info("Created trainee with id: {}", id);
        return trainee;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        log.debug("Finding trainee by id: {}", id);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        log.debug("Finding trainee by username: {}", username);

        return storage.values().stream()
                .filter(trainee -> username.equals(trainee.getUsername()))
                .findFirst();
    }

    @Override
    public List<Trainee> findAll() {
        log.debug("Finding all trainees");
        return new ArrayList<>(storage.values());
    }

    @Override
    public Trainee update(Trainee trainee) {
        log.debug("Updating trainee: {}", trainee);

        if (trainee.getId() == null || !storage.containsKey(trainee.getId())) {
            log.error("Trainee not found for update: {}", trainee.getId());
            throw new EntityNotFoundException("Trainee", trainee.getId());
        }

        storage.put(trainee.getId(), trainee);
        log.info("Updated trainee with id: {}", trainee.getId());
        return trainee;
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting trainee with id: {}", id);

        if (!storage.containsKey(id)) {
            log.error("Trainee not found for delete: {}", id);
            throw new EntityNotFoundException("Trainee", id);
        }

        storage.remove(id);
        log.info("Deleted trainee with id: {}", id);
    }
}
