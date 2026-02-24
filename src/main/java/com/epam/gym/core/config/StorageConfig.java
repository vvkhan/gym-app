package com.epam.gym.core.config;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.epam.gym.core.storage.InitialData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Config class for in-memory storage
 */
@Configuration
public class StorageConfig {

    private static final Logger log = LoggerFactory.getLogger(StorageConfig.class);

    @Bean("traineeStorage")
    public Map<Long, Trainee> traineeStorage() { return new HashMap<>(); }

    @Bean("trainerStorage")
    public Map<Long, Trainer> trainerStorage() { return new HashMap<>(); }

    @Bean("trainingStorage")
    public Map<Long, Training> trainingStorage() { return new HashMap<>(); }

    @Bean("trainingTypeStorage")
    public Map<Long, TrainingType> trainingTypeStorage() { return new HashMap<>(); }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    @Bean
    public InitialData initialData(
            ObjectMapper objectMapper,
            @Value("${storage.data.file.path}") Resource dataFile) {
        if (!dataFile.exists()) {
            log.debug("Initial data file not found: {}. Starting with empty storage.", dataFile.getDescription());
            return new InitialData();
        }
        try (InputStream inputStream = dataFile.getInputStream()) {
            InitialData data = objectMapper.readValue(inputStream, InitialData.class);
            log.debug("Loaded initial data from: {}", dataFile.getDescription());
            return data;
        } catch (IOException e) {
            log.error("Failed to load initial data from: {}", dataFile.getDescription());
            log.debug("Stack trace:", e);
            throw new UncheckedIOException(e);
        }
    }
}
