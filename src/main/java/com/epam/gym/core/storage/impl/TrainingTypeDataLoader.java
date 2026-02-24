package com.epam.gym.core.storage.impl;

import com.epam.gym.core.model.TrainingType;
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
public class TrainingTypeDataLoader implements StorageDataLoader {

    private Map<Long, TrainingType> storage;
    private InitialData initialData;

    @Autowired
    public void setStorage(@Qualifier("trainingTypeStorage") Map<Long, TrainingType> storage) {
        this.storage = storage;
    }

    @Autowired
    public void setInitialData(InitialData initialData) {
        this.initialData = initialData;
    }

    @PostConstruct
    @Override
    public void load() {
        if (initialData.getTrainingTypes() == null) return;
        initialData.getTrainingTypes().forEach(type -> {
            validate(type);
            storage.put(type.getId(), type);
        });
    }

    private void validate(TrainingType t) {
        if (t.getId() == null) {
            throw new IllegalStateException("TrainingType: id is null");
        }
        if (t.getTrainingTypeName() == null || t.getTrainingTypeName().isBlank()) {
            throw new IllegalStateException("TrainingType id=" + t.getId() + ": name is blank");
        }
    }
}
