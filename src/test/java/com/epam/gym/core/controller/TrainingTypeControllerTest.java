package com.epam.gym.core.controller;

import com.epam.gym.core.dto.response.TrainingTypeResponse;
import com.epam.gym.core.exception.handler.RestExceptionHandler;
import com.epam.gym.core.facade.GymFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerTest {

    @Mock
    private GymFacade facade;

    @InjectMocks
    private TrainingTypeController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .build();
    }

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    @Test
    void getAll_validTraineeReturnsTypes() throws Exception {
        when(facade.authenticateTrainee("alice", "pass")).thenReturn(true);

        TrainingTypeResponse yoga = new TrainingTypeResponse();
        yoga.setId(UUID.randomUUID());
        yoga.setTrainingTypeName("Yoga");

        TrainingTypeResponse fitness = new TrainingTypeResponse();
        fitness.setId(UUID.randomUUID());
        fitness.setTrainingTypeName("Fitness");

        when(facade.getAllTrainingTypes()).thenReturn(List.of(yoga, fitness));

        mockMvc.perform(get("/api/training-types")
                        .header("Authorization", basicAuth("alice", "pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingTypeName").value("Yoga"))
                .andExpect(jsonPath("$[1].trainingTypeName").value("Fitness"));
    }

    @Test
    void getAll_validTrainerReturnsTypes() throws Exception {
        when(facade.authenticateTrainee("john", "pass")).thenReturn(false);
        when(facade.authenticateTrainer("john", "pass")).thenReturn(true);
        when(facade.getAllTrainingTypes()).thenReturn(List.of());

        mockMvc.perform(get("/api/training-types")
                        .header("Authorization", basicAuth("john", "pass")))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_missingAuthHeaderReturns401() throws Exception {
        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isUnauthorized());
    }
}
