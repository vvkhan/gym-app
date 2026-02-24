package com.epam.gym.core.storage.impl;

import com.epam.gym.core.model.Training;
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
public class TrainingDataLoader implements StorageDataLoader {

    private Map<Long, Training> storage;
    private InitialData initialData;

    @Autowired
    public void setStorage(@Qualifier("trainingStorage") Map<Long, Training> storage) {
        this.storage = storage;
    }

    @Autowired
    public void setInitialData(InitialData initialData) {
        this.initialData = initialData;
    }

    @PostConstruct
    @Override
    public void load() {
        if (initialData.getTrainings() == null) return;
        initialData.getTrainings().forEach(training -> {
            validate(training);
            storage.put(training.getId(), training);
        });
    }

    private void validate(Training t) {
        if (t.getId() == null) {
            throw new IllegalStateException("Training: id is null");
        }
        if (t.getTraineeId() == null) {
            throw new IllegalStateException("Training id=" + t.getId() + ": traineeId is null");
        }
        if (t.getTrainerId() == null) {
            throw new IllegalStateException("Training id=" + t.getId() + ": trainerId is null");
        }
        if (t.getTrainingName() == null || t.getTrainingName().isBlank()) {
            throw new IllegalStateException("Training id=" + t.getId() + ": trainingName is blank");
        }
        if (t.getTrainingType() == null) {
            throw new IllegalStateException("Training id=" + t.getId() + ": trainingType is null");
        }
        if (t.getTrainingDate() == null) {
            throw new IllegalStateException("Training id=" + t.getId() + ": trainingDate is null");
        }
        if (t.getDuration() == null || t.getDuration() <= 0) {
            throw new IllegalStateException("Training id=" + t.getId() + ": duration must be positive");
        }
    }
}
