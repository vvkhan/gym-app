package com.epam.gym.core.controller;

import com.epam.gym.core.dto.request.AddTrainingRequest;
import com.epam.gym.core.exception.handler.RestExceptionHandler;
import com.epam.gym.core.facade.GymFacade;
import com.epam.gym.core.controller.AuthenticationHelper;
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
import java.util.Base64;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private GymFacade facade;

    @Mock
    private AuthenticationHelper authHelper;

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

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    @Test
    void addTraining_validRequestReturns200() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("alice");
        request.setTrainerUsername("john");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(LocalDate.of(2024, 6, 15));
        request.setDuration(60);

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuth("alice", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).createTraining("alice", "john", "Morning Yoga",
                LocalDate.of(2024, 6, 15), 60);
    }

    @Test
    void addTraining_missingAuthHeaderReturns401() throws Exception {
        doThrow(new SecurityException("Missing Authorization header"))
                .when(authHelper).authenticateTrainee(any(), any());

        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("alice");
        request.setTrainerUsername("john");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(LocalDate.of(2024, 6, 15));
        request.setDuration(60);

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addTraining_invalidCredentialsReturns401() throws Exception {
        doThrow(new SecurityException("Invalid username or password"))
                .when(authHelper).authenticateTrainee(any(), any());

        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("alice");
        request.setTrainerUsername("john");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(LocalDate.of(2024, 6, 15));
        request.setDuration(60);

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuth("alice", "wrong"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addTraining_missingTrainingNameReturns400() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("alice");
        request.setTrainerUsername("john");
        request.setTrainingDate(LocalDate.of(2024, 6, 15));
        request.setDuration(60);

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuth("alice", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_differentTraineeReturns401() throws Exception {
        doThrow(new SecurityException("Access denied"))
                .when(authHelper).authenticateTrainee(any(), any());

        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("alice");
        request.setTrainerUsername("john");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(LocalDate.of(2024, 6, 15));
        request.setDuration(60);

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuth("bob", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addTraining_traineeNotFoundReturns404() throws Exception {
        doThrow(new NoSuchElementException("Trainee not found: alice"))
                .when(facade).createTraining("alice", "john", "Morning Yoga",
                        LocalDate.of(2024, 6, 15), 60);

        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("alice");
        request.setTrainerUsername("john");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(LocalDate.of(2024, 6, 15));
        request.setDuration(60);

        mockMvc.perform(post("/api/trainings")
                        .header("Authorization", basicAuth("alice", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
