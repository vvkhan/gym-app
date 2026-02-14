package com.epam.gym.core.storage;

import com.epam.gym.core.exception.StorageException;
import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.model.Training;
import com.epam.gym.core.model.TrainingType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageInitializerTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    private StorageInitializer storageInitializer;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        storageInitializer = new StorageInitializer(objectMapper, resourceLoader, "classpath:test-data.json");
    }

    private void mockResource(String json) throws IOException {
        when(resourceLoader.getResource(any())).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(json.getBytes()));
    }

    @Test
    void postProcessAfterInitialization_TrainingTypeStoragePopulatesMap() throws IOException {
        mockResource("{\"trainees\":[],\"trainers\":[],\"trainings\":[]," +
                "\"trainingTypes\":[{\"id\":1,\"trainingTypeName\":\"Cardio\"},{\"id\":2,\"trainingTypeName\":\"Strength\"}]}");

        Map<Long, TrainingType> storage = new HashMap<>();
        storageInitializer.postProcessAfterInitialization(storage, "trainingTypeStorage");

        assertEquals(2, storage.size());
        assertEquals("Cardio", storage.get(1L).getTrainingTypeName());
        assertEquals("Strength", storage.get(2L).getTrainingTypeName());
    }

    @Test
    void postProcessAfterInitialization_TraineeStoragePopulatesMap() throws IOException {
        mockResource("{\"trainees\":[{\"id\":1,\"firstName\":\"John\",\"lastName\":\"Doe\"," +
                "\"username\":\"John.Doe\",\"password\":\"pass123\",\"isActive\":true," +
                "\"dateOfBirth\":\"1990-01-01\",\"address\":\"123 Main St\"}]," +
                "\"trainers\":[],\"trainings\":[],\"trainingTypes\":[]}");

        Map<Long, Trainee> storage = new HashMap<>();
        storageInitializer.postProcessAfterInitialization(storage, "traineeStorage");

        assertEquals(1, storage.size());
        assertEquals("John", storage.get(1L).getFirstName());
        assertEquals("John.Doe", storage.get(1L).getUsername());
    }

    @Test
    void postProcessAfterInitialization_TrainerStoragePopulatesMap() throws IOException {
        mockResource("{\"trainees\":[],\"trainings\":[],\"trainingTypes\":[]," +
                "\"trainers\":[{\"id\":1,\"firstName\":\"Jane\",\"lastName\":\"Smith\"," +
                "\"username\":\"Jane.Smith\",\"password\":\"pass123\",\"isActive\":true," +
                "\"specialization\":{\"id\":1,\"trainingTypeName\":\"Cardio\"}}]}");

        Map<Long, Trainer> storage = new HashMap<>();
        storageInitializer.postProcessAfterInitialization(storage, "trainerStorage");

        assertEquals(1, storage.size());
        assertEquals("Jane", storage.get(1L).getFirstName());
        assertEquals("Cardio", storage.get(1L).getSpecialization().getTrainingTypeName());
    }

    @Test
    void postProcessAfterInitialization_TrainingStoragePopulatesMap() throws IOException {
        mockResource("{\"trainees\":[],\"trainers\":[],\"trainingTypes\":[]," +
                "\"trainings\":[{\"id\":1,\"traineeId\":1,\"trainerId\":1," +
                "\"trainingName\":\"Morning Session\"," +
                "\"trainingType\":{\"id\":1,\"trainingTypeName\":\"Cardio\"}," +
                "\"trainingDate\":\"2024-01-15\",\"duration\":60}]}");

        Map<Long, Training> storage = new HashMap<>();
        storageInitializer.postProcessAfterInitialization(storage, "trainingStorage");

        assertEquals(1, storage.size());
        assertEquals("Morning Session", storage.get(1L).getTrainingName());
        assertEquals(60, storage.get(1L).getDuration());
    }

    @Test
    void postProcessAfterInitialization_DataLoadedOnlyOnce() throws IOException {
        mockResource("{\"trainees\":[],\"trainers\":[],\"trainings\":[],\"trainingTypes\":[]}");

        storageInitializer.postProcessAfterInitialization(new HashMap<>(), "traineeStorage");
        storageInitializer.postProcessAfterInitialization(new HashMap<>(), "trainerStorage");
        storageInitializer.postProcessAfterInitialization(new HashMap<>(), "trainingTypeStorage");

        verify(resourceLoader, times(1)).getResource(any());
    }

    @Test
    void postProcessAfterInitialization_MissingDataFileLeavesStorageEmpty() throws IOException {
        when(resourceLoader.getResource(any())).thenReturn(resource);
        when(resource.exists()).thenReturn(false);

        Map<Long, TrainingType> storage = new HashMap<>();
        storageInitializer.postProcessAfterInitialization(storage, "trainingTypeStorage");

        assertTrue(storage.isEmpty());
    }
}
