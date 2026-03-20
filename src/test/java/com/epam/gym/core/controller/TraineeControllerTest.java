package com.epam.gym.core.controller;

import com.epam.gym.core.dto.request.ActivateDeactivateRequest;
import com.epam.gym.core.dto.request.TraineeRegistrationRequest;
import com.epam.gym.core.dto.request.TraineeTrainersUpdateRequest;
import com.epam.gym.core.dto.request.UpdateTraineeRequest;
import com.epam.gym.core.dto.response.RegistrationResponse;
import com.epam.gym.core.dto.response.TraineeProfileResponse;
import com.epam.gym.core.dto.response.TrainerSummaryResponse;
import com.epam.gym.core.dto.response.TrainingResponse;
import com.epam.gym.core.dto.response.UpdatedTraineeProfileResponse;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    private GymFacade facade;

    @Mock
    private AuthenticationHelper authHelper;

    @InjectMocks
    private TraineeController controller;

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
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("Alice");
        request.setLastName("Smith");

        when(facade.registerTrainee("Alice", "Smith", null, null))
                .thenReturn(new RegistrationResponse("Alice.Smith", "pass123"));

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Alice.Smith"));
    }

    @Test
    void register_missingFirstNameReturns400() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setLastName("Smith");

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Get profile

    @Test
    void getProfile_validAuthReturns200() throws Exception {
        when(facade.getTraineeByUsername("alice")).thenReturn(new TraineeProfileResponse());

        mockMvc.perform(get("/api/trainees/alice")
                        .header("Authorization", basicAuth("alice", "pass")))
                .andExpect(status().isOk());
    }

    @Test
    void getProfile_missingAuthHeaderReturns401() throws Exception {
        doThrow(new SecurityException("Missing Authorization header"))
                .when(authHelper).authenticateOwner(any(), any());

        mockMvc.perform(get("/api/trainees/alice"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_differentUserReturns401() throws Exception {
        doThrow(new SecurityException("Access denied"))
                .when(authHelper).authenticateOwner(any(), any());

        mockMvc.perform(get("/api/trainees/alice")
                        .header("Authorization", basicAuth("bob", "pass")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_wrongCredentialsReturns401() throws Exception {
        doThrow(new SecurityException("Invalid username or password"))
                .when(authHelper).authenticateOwner(any(), any());

        mockMvc.perform(get("/api/trainees/alice")
                        .header("Authorization", basicAuth("alice", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_traineeNotFoundReturns404() throws Exception {
        when(facade.getTraineeByUsername("ghost"))
                .thenThrow(new NoSuchElementException("Trainee not found: ghost"));

        mockMvc.perform(get("/api/trainees/ghost")
                        .header("Authorization", basicAuth("ghost", "pass")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Trainee not found: ghost"));
    }

    // Update profile

    @Test
    void updateProfile_validRequestReturns200() throws Exception {
        when(facade.updateTraineeProfile(eq("alice"), any(), any(), any(), any(), any()))
                .thenReturn(new UpdatedTraineeProfileResponse());

        UpdateTraineeRequest request = new UpdateTraineeRequest();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setIsActive(true);

        mockMvc.perform(put("/api/trainees/alice")
                        .header("Authorization", basicAuth("alice", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateProfile_missingIsActiveReturns400() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest();
        request.setFirstName("Alice");
        request.setLastName("Smith");

        mockMvc.perform(put("/api/trainees/alice")
                        .header("Authorization", basicAuth("alice", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Delete

    @Test
    void delete_validAuthReturns200() throws Exception {
        mockMvc.perform(delete("/api/trainees/alice")
                        .header("Authorization", basicAuth("alice", "pass")))
                .andExpect(status().isOk());

        verify(facade).deleteTrainee("alice");
    }

    // Activate / Deactivate

    @Test
    void activate_isActiveTrueCallsActivate() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setIsActive(true);

        mockMvc.perform(patch("/api/trainees/alice/activate")
                        .header("Authorization", basicAuth("alice", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).activateTrainee("alice");
    }

    @Test
    void activate_isActiveFalseCallsDeactivate() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setIsActive(false);

        mockMvc.perform(patch("/api/trainees/alice/activate")
                        .header("Authorization", basicAuth("alice", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).deactivateTrainee("alice");
    }

    @Test
    void activate_alreadyActiveReturns409() throws Exception {
        doThrow(new IllegalStateException("Trainee is already active: alice"))
                .when(facade).activateTrainee("alice");

        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setIsActive(true);

        mockMvc.perform(patch("/api/trainees/alice/activate")
                        .header("Authorization", basicAuth("alice", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // Not assigned trainers

    @Test
    void getNotAssignedTrainers_validAuthReturns200() throws Exception {
        when(facade.getNotAssignedTrainers("alice")).thenReturn(List.of(new TrainerSummaryResponse()));

        mockMvc.perform(get("/api/trainees/alice/not-assigned-trainers")
                        .header("Authorization", basicAuth("alice", "pass")))
                .andExpect(status().isOk());
    }

    // Update trainers list

    @Test
    void updateTrainers_validRequestReturns200() throws Exception {
        when(facade.updateTraineeTrainers(eq("alice"), any(Set.class)))
                .thenReturn(List.of(new TrainerSummaryResponse()));

        TraineeTrainersUpdateRequest request = new TraineeTrainersUpdateRequest();
        request.setTrainerUsernames(List.of("trainer1", "trainer2"));

        mockMvc.perform(put("/api/trainees/alice/trainers")
                        .header("Authorization", basicAuth("alice", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateTrainers_emptyListReturns400() throws Exception {
        TraineeTrainersUpdateRequest request = new TraineeTrainersUpdateRequest();
        request.setTrainerUsernames(List.of());

        mockMvc.perform(put("/api/trainees/alice/trainers")
                        .header("Authorization", basicAuth("alice", "pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Trainings list

    @Test
    void getTrainings_noFiltersReturns200() throws Exception {
        when(facade.getTraineeTrainings("alice", null, null, null, null))
                .thenReturn(List.of(new TrainingResponse()));

        mockMvc.perform(get("/api/trainees/alice/trainings")
                        .header("Authorization", basicAuth("alice", "pass")))
                .andExpect(status().isOk());
    }

    @Test
    void getTrainings_withFiltersReturns200() throws Exception {
        when(facade.getTraineeTrainings(eq("alice"), any(), any(), eq("John"), eq("Yoga")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainees/alice/trainings")
                        .header("Authorization", basicAuth("alice", "pass"))
                        .param("periodFrom", "2024-01-01")
                        .param("periodTo", "2024-12-31")
                        .param("trainerUsername", "John")
                        .param("trainingType", "Yoga"))
                .andExpect(status().isOk());
    }
}
