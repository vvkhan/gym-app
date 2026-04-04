package com.epam.gym.core.controller;

import com.epam.gym.core.dto.request.AddTrainingRequest;
import com.epam.gym.core.exception.handler.RestExceptionHandler;
import com.epam.gym.core.facade.GymFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private GymFacade facade;

    @InjectMocks
    private TrainingController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(validator)
                .build();
    }

    @Test
    void addTraining_ValidRequestReturns200() throws Exception {
        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk());

        verify(facade).createTraining("Alice.Smith", "John.Smith", "Morning Yoga",
                LocalDate.of(2024, 6, 15), 60);
    }

    @Test
    void addTraining_MissingTrainingNameReturns400() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("Alice.Smith");
        request.setTrainerUsername("John.Smith");
        request.setTrainingDate(LocalDate.of(2024, 6, 15));
        request.setDuration(60);

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_TraineeNotFoundReturns404() throws Exception {
        doThrow(new NoSuchElementException("Trainee not found: Alice.Smith"))
                .when(facade).createTraining("Alice.Smith", "John.Smith", "Morning Yoga",
                        LocalDate.of(2024, 6, 15), 60);

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    // Helper

    private AddTrainingRequest validRequest() {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("Alice.Smith");
        request.setTrainerUsername("John.Smith");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(LocalDate.of(2024, 6, 15));
        request.setDuration(60);
        return request;
    }
}
