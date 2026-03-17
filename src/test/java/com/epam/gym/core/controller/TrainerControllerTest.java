package com.epam.gym.core.controller;

import com.epam.gym.core.dto.request.ActivateDeactivateRequest;
import com.epam.gym.core.dto.request.TrainerRegistrationRequest;
import com.epam.gym.core.dto.request.UpdateTrainerRequest;
import com.epam.gym.core.dto.response.RegistrationResponse;
import com.epam.gym.core.dto.response.TrainerProfileResponse;
import com.epam.gym.core.dto.response.TrainingResponse;
import com.epam.gym.core.dto.response.UpdatedTrainerProfileResponse;
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

import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock
    private GymFacade facade;

    @Mock
    private AuthenticationHelper authHelper;

    @InjectMocks
    private TrainerController controller;

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

    // Registration

    @Test
    void register_validRequestReturns201() throws Exception {
        UUID specializationId = UUID.randomUUID();

        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setSpecializationId(specializationId);

        when(facade.registerTrainer("John", "Smith", specializationId))
                .thenReturn(new RegistrationResponse("John.Smith", "pass123"));

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("John.Smith"));
    }

    @Test
    void register_missingSpecializationReturns400() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Smith");

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_specializationNotFoundReturns404() throws Exception {
        UUID unknownId = UUID.randomUUID();

        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setSpecializationId(unknownId);

        when(facade.registerTrainer("John", "Smith", unknownId))
                .thenThrow(new NoSuchElementException("TrainingType not found"));

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // Get profile

    @Test
    void getProfile_validAuthReturns200() throws Exception {
        when(facade.getTrainerByUsername("john")).thenReturn(new TrainerProfileResponse());

        mockMvc.perform(get("/api/trainers/john")
                        .header("Authorization", basicAuth("john", "pass")))
                .andExpect(status().isOk());
    }

    @Test
    void getProfile_missingAuthHeaderReturns401() throws Exception {
        doThrow(new SecurityException("Missing Authorization header"))
                .when(authHelper).authenticateOwner(any(), any());

        mockMvc.perform(get("/api/trainers/john"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_differentUserReturns401() throws Exception {
        doThrow(new SecurityException("Access denied"))
                .when(authHelper).authenticateOwner(any(), any());

        mockMvc.perform(get("/api/trainers/john")
                        .header("Authorization", basicAuth("alice", "pass")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_trainerNotFoundReturns404() throws Exception {
        when(facade.getTrainerByUsername("ghost"))
                .thenThrow(new NoSuchElementException("Trainer not found: ghost"));

        mockMvc.perform(get("/api/trainers/ghost")
                        .header("Authorization", basicAuth("ghost", "pass")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Trainer not found: ghost"));
    }

    // Update profile

    @Test
    void updateProfile_validRequestReturns200() throws Exception {
        when(facade.updateTrainerProfile(eq("john"), any(), any(), any()))
                .thenReturn(new UpdatedTrainerProfileResponse());

        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setIsActive(true);

        mockMvc.perform(put("/api/trainers/john")
                        .header("Authorization", basicAuth("john", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateProfile_missingFirstNameReturns400() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setLastName("Smith");
        request.setIsActive(true);

        mockMvc.perform(put("/api/trainers/john")
                        .header("Authorization", basicAuth("john", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Activate / Deactivate

    @Test
    void activate_isActiveTrueCallsActivate() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setIsActive(true);

        mockMvc.perform(patch("/api/trainers/john/activate")
                        .header("Authorization", basicAuth("john", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).activateTrainer("john");
    }

    @Test
    void activate_isActiveFalseCallsDeactivate() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setIsActive(false);

        mockMvc.perform(patch("/api/trainers/john/activate")
                        .header("Authorization", basicAuth("john", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).deactivateTrainer("john");
    }

    @Test
    void activate_alreadyInactiveReturns409() throws Exception {
        doThrow(new IllegalStateException("Trainer is already inactive: john"))
                .when(facade).deactivateTrainer("john");

        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setIsActive(false);

        mockMvc.perform(patch("/api/trainers/john/activate")
                        .header("Authorization", basicAuth("john", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // Trainings list

    @Test
    void getTrainings_noFiltersReturns200() throws Exception {
        when(facade.getTrainerTrainings("john", null, null, null))
                .thenReturn(List.of(new TrainingResponse()));

        mockMvc.perform(get("/api/trainers/john/trainings")
                        .header("Authorization", basicAuth("john", "pass")))
                .andExpect(status().isOk());
    }

    @Test
    void getTrainings_withTraineeNameReturns200() throws Exception {
        when(facade.getTrainerTrainings(eq("john"), any(), any(), eq("Alice")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainers/john/trainings")
                        .header("Authorization", basicAuth("john", "pass"))
                        .param("traineeUsername", "Alice"))
                .andExpect(status().isOk());
    }
}
