package com.epam.gym.core.config;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Config class for in-memory storage
 */
@Configuration
public class StorageConfig {

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
}
