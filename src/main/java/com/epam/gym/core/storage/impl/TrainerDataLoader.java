package com.epam.gym.core.storage.impl;

import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.storage.InitialData;
import com.epam.gym.core.storage.StorageDataLoader;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TrainerDataLoader implements StorageDataLoader {

    private Map<Long, Trainer> storage;
    private InitialData initialData;

    @Autowired
    public void setStorage(@Qualifier("trainerStorage") Map<Long, Trainer> storage) {
        this.storage = storage;
    }

    @Autowired
    public void setInitialData(InitialData initialData) {
        this.initialData = initialData;
    }

    @PostConstruct
    @Override
    public void load() {
        if (initialData.getTrainers() == null) return;
        initialData.getTrainers().forEach(trainer -> {
            validate(trainer);
            storage.put(trainer.getId(), trainer);
        });
    }

    private void validate(Trainer t) {
        if (t.getId() == null) {
            throw new IllegalStateException("Trainer: id is null");
        }
        if (t.getFirstName() == null || t.getFirstName().isBlank()) {
            throw new IllegalStateException("Trainer id=" + t.getId() + ": firstName is blank");
        }
        if (t.getLastName() == null || t.getLastName().isBlank()) {
            throw new IllegalStateException("Trainer id=" + t.getId() + ": lastName is blank");
        }
        if (t.getUsername() == null || t.getUsername().isBlank()) {
            throw new IllegalStateException("Trainer id=" + t.getId() + ": username is blank");
        }
        if (t.getIsActive() == null) {
            throw new IllegalStateException("Trainer id=" + t.getId() + ": isActive is null");
        }
        if (t.getSpecialization() == null) {
            throw new IllegalStateException("Trainer id=" + t.getId() + ": specialization is null");
        }
    }
}
