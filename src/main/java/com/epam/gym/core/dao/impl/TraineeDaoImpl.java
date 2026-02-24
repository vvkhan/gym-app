package com.epam.gym.core.dao.impl;

import com.epam.gym.core.dao.TraineeDao;
import java.util.NoSuchElementException;
import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.aspect.LogExecution;
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
@DependsOn("traineeDataLoader")
public class TraineeDaoImpl implements TraineeDao {

    private Map<Long, Trainee> storage;
    private long currentId = 1;

    @Autowired
    public void setStorage(@Qualifier("traineeStorage") Map<Long, Trainee> storage) {
        this.storage = storage;
    }

    @PostConstruct
    public void initCurrentId() {
        if (!storage.isEmpty()) {
            currentId = Collections.max(storage.keySet()) + 1;
        }
    }

    @Override
    public Trainee create(Trainee trainee) {
        Long id = currentId++;
        Trainee stored = Trainee.builder()
                .id(id)
                .firstName(trainee.getFirstName())
                .lastName(trainee.getLastName())
                .username(trainee.getUsername())
                .password(trainee.getPassword())
                .isActive(trainee.getIsActive())
                .dateOfBirth(trainee.getDateOfBirth())
                .address(trainee.getAddress())
                .build();
        storage.put(id, stored);
        return copy(stored);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        return Optional.ofNullable(storage.get(id)).map(this::copy);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        return storage.values().stream()
                .filter(trainee -> username.equals(trainee.getUsername()))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public List<Trainee> findAll() {
        return storage.values().stream()
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public Trainee update(Trainee trainee) {
        if (trainee.getId() == null || !storage.containsKey(trainee.getId())) {
            throw new NoSuchElementException("Trainee with id " + trainee.getId() + " not found");
        }
        Trainee stored = copy(trainee);
        storage.put(stored.getId(), stored);
        return copy(stored);
    }

    @Override
    public void delete(Long id) {
        Trainee removed = storage.remove(id);
        if (removed == null) {
            throw new NoSuchElementException("Trainee with id " + id + " not found");
        }
    }

    private Trainee copy(Trainee t) {
        return t.toBuilder().build();
    }
}
