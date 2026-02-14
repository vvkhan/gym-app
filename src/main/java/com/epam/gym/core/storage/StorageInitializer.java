package com.epam.gym.core.storage;

import com.epam.gym.core.exception.StorageException;
import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Initializes storage beans with data from JSON file.
 * Loads data from the file specified in application.properties on startup.
 */
@Component
public class StorageInitializer implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String dataFilePath;

    private InitialData initialData;
    private boolean dataLoaded = false;

    @Autowired
    public StorageInitializer(
            ObjectMapper objectMapper,
            ResourceLoader resourceLoader,
            @Value("${storage.data.file.path}") String dataFilePath) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.dataFilePath = dataFilePath;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // load data once when first storage is encountered
        if (!dataLoaded && isStorageBean(beanName)) {
            loadInitialData();
            dataLoaded = true;
        }

        // populate specific storage
        if (initialData != null) {
            populateStorage(bean, beanName);
        }

        return bean;
    }

    private boolean isStorageBean(String beanName) {
        return beanName.equals("traineeStorage") ||
                beanName.equals("trainerStorage") ||
                beanName.equals("trainingStorage") ||
                beanName.equals("trainingTypeStorage");
    }

    private void loadInitialData() {
        try {
            log.info("Loading initial data from: {}", dataFilePath);
            Resource resource = resourceLoader.getResource(dataFilePath);

            if (!resource.exists()) {
                log.warn("Initial data file not found: {}. Starting with empty storage.", dataFilePath);
                initialData = new InitialData();
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                initialData = objectMapper.readValue(inputStream, InitialData.class);
                log.info("Successfully loaded initial data: {} trainees, {} trainers, {} trainings, {} training types",
                        initialData.getTrainees() != null ? initialData.getTrainees().size() : 0,
                        initialData.getTrainers() != null ? initialData.getTrainers().size() : 0,
                        initialData.getTrainings() != null ? initialData.getTrainings().size() : 0,
                        initialData.getTrainingTypes() != null ? initialData.getTrainingTypes().size() : 0);
            }
        } catch (IOException e) {
            log.error("Failed to load initial data from: {}", dataFilePath, e);
            throw new StorageException("Failed to load initial data", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void populateStorage(Object bean, String beanName) {
        if (!(bean instanceof Map)) {
            return;
        }

        Map<Long, Object> storage = (Map<Long, Object>) bean;

        switch (beanName) {
            case "traineeStorage":
                if (initialData.getTrainees() != null) {
                    initialData.getTrainees().forEach(trainee ->
                            storage.put(trainee.getId(), trainee));
                    log.debug("Populated traineeStorage with {} entries", initialData.getTrainees().size());
                }
                break;

            case "trainerStorage":
                if (initialData.getTrainers() != null) {
                    initialData.getTrainers().forEach(trainer ->
                            storage.put(trainer.getId(), trainer));
                    log.debug("Populated trainerStorage with {} entries", initialData.getTrainers().size());
                }
                break;

            case "trainingStorage":
                if (initialData.getTrainings() != null) {
                    initialData.getTrainings().forEach(training ->
                            storage.put(training.getId(), training));
                    log.debug("Populated trainingStorage with {} entries", initialData.getTrainings().size());
                }
                break;

            case "trainingTypeStorage":
                if (initialData.getTrainingTypes() != null) {
                    initialData.getTrainingTypes().forEach(trainingType ->
                            storage.put(trainingType.getId(), trainingType));
                    log.debug("Populated trainingTypeStorage with {} entries", initialData.getTrainingTypes().size());
                }
                break;
        }
    }

    // DTO for deserializing initial data from JSON
    public static class InitialData {
        private List<Trainee> trainees;
        private List<Trainer> trainers;
        private List<Training> trainings;
        private List<TrainingType> trainingTypes;

        public List<Trainee> getTrainees() {
            return trainees;
        }

        public void setTrainees(List<Trainee> trainees) {
            this.trainees = trainees;
        }

        public List<Trainer> getTrainers() {
            return trainers;
        }

        public void setTrainers(List<Trainer> trainers) {
            this.trainers = trainers;
        }

        public List<Training> getTrainings() {
            return trainings;
        }

        public void setTrainings(List<Training> trainings) {
            this.trainings = trainings;
        }

        public List<TrainingType> getTrainingTypes() {
            return trainingTypes;
        }

        public void setTrainingTypes(List<TrainingType> trainingTypes) {
            this.trainingTypes = trainingTypes;
        }
    }
}
