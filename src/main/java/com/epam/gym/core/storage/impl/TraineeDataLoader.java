package com.epam.gym.core.storage.impl;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.storage.InitialData;
import com.epam.gym.core.storage.StorageDataLoader;
import com.epam.gym.core.aspect.LogExecution;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@LogExecution
@Component
public class TraineeDataLoader implements StorageDataLoader {

    private Map<Long, Trainee> storage;
    private InitialData initialData;

    @Autowired
    public void setStorage(@Qualifier("traineeStorage") Map<Long, Trainee> storage) {
        this.storage = storage;
    }

    @Autowired
    public void setInitialData(InitialData initialData) {
        this.initialData = initialData;
    }

    @PostConstruct
    @Override
    public void load() {
        if (initialData.getTrainees() == null) return;
        initialData.getTrainees().forEach(trainee -> {
            validate(trainee);
            storage.put(trainee.getId(), trainee);
        });
    }

    private void validate(Trainee t) {
        if (t.getId() == null) {
            throw new IllegalStateException("Trainee: id is null");
        }
        if (t.getFirstName() == null || t.getFirstName().isBlank()) {
            throw new IllegalStateException("Trainee id=" + t.getId() + ": firstName is blank");
        }
        if (t.getLastName() == null || t.getLastName().isBlank()) {
            throw new IllegalStateException("Trainee id=" + t.getId() + ": lastName is blank");
        }
        if (t.getUsername() == null || t.getUsername().isBlank()) {
            throw new IllegalStateException("Trainee id=" + t.getId() + ": username is blank");
        }
        if (t.getIsActive() == null) {
            throw new IllegalStateException("Trainee id=" + t.getId() + ": isActive is null");
        }
    }
}
