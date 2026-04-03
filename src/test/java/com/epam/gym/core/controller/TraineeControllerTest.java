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
import com.epam.gym.core.model.User;
import com.epam.gym.core.model.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
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
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new RestExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(validator)
                .build();
        setSecurityContext("Alice.Smith");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // Registration (public — no principal needed)

    @Test
    void register_ValidRequestReturns201() throws Exception {
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
    void register_MissingFirstNameReturns400() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setLastName("Smith");

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Get profile

    @Test
    void getProfile_UsesAuthenticatedUsername() throws Exception {
        when(facade.getTraineeByUsername("Alice.Smith")).thenReturn(new TraineeProfileResponse());

        mockMvc.perform(get("/api/trainees/Alice.Smith"))
                .andExpect(status().isOk());

        verify(facade).getTraineeByUsername("Alice.Smith");
    }

    @Test
    void getProfile_TraineeNotFoundReturns404() throws Exception {
        when(facade.getTraineeByUsername("Alice.Smith"))
                .thenThrow(new NoSuchElementException("Trainee not found: Alice.Smith"));

        mockMvc.perform(get("/api/trainees/Alice.Smith"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Trainee not found: Alice.Smith"));
    }

    // Update profile

    @Test
    void updateProfile_UsesAuthenticatedUsername() throws Exception {
        when(facade.updateTraineeProfile(eq("Alice.Smith"), any(), any(), any(), any(), any()))
                .thenReturn(new UpdatedTraineeProfileResponse());

        UpdateTraineeRequest request = new UpdateTraineeRequest();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setIsActive(true);

        mockMvc.perform(put("/api/trainees/Alice.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).updateTraineeProfile(eq("Alice.Smith"), any(), any(), any(), any(), any());
    }

    @Test
    void updateProfile_MissingIsActiveReturns400() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest();
        request.setFirstName("Alice");
        request.setLastName("Smith");

        mockMvc.perform(put("/api/trainees/Alice.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Delete

    @Test
    void delete_UsesAuthenticatedUsername() throws Exception {
        mockMvc.perform(delete("/api/trainees/Alice.Smith"))
                .andExpect(status().isOk());

        verify(facade).deleteTrainee("Alice.Smith");
    }

    // Activate / Deactivate

    @Test
    void activate_IsActiveTrueCallsActivate() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setIsActive(true);

        mockMvc.perform(patch("/api/trainees/Alice.Smith/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).activateTrainee("Alice.Smith");
    }

    @Test
    void activate_IsActiveFalseCallsDeactivate() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setIsActive(false);

        mockMvc.perform(patch("/api/trainees/Alice.Smith/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).deactivateTrainee("Alice.Smith");
    }

    @Test
    void activate_AlreadyActiveReturns409() throws Exception {
        doThrow(new IllegalStateException("Trainee is already active: Alice.Smith"))
                .when(facade).activateTrainee("Alice.Smith");

        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setIsActive(true);

        mockMvc.perform(patch("/api/trainees/Alice.Smith/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // Not assigned trainers

    @Test
    void getNotAssignedTrainers_UsesAuthenticatedUsername() throws Exception {
        when(facade.getNotAssignedTrainers("Alice.Smith")).thenReturn(List.of(new TrainerSummaryResponse()));

        mockMvc.perform(get("/api/trainees/Alice.Smith/not-assigned-trainers"))
                .andExpect(status().isOk());

        verify(facade).getNotAssignedTrainers("Alice.Smith");
    }

    // Update trainers list

    @Test
    void updateTrainers_UsesAuthenticatedUsername() throws Exception {
        when(facade.updateTraineeTrainers(eq("Alice.Smith"), any(Set.class)))
                .thenReturn(List.of(new TrainerSummaryResponse()));

        TraineeTrainersUpdateRequest request = new TraineeTrainersUpdateRequest();
        request.setTrainerUsernames(List.of("trainer1", "trainer2"));

        mockMvc.perform(put("/api/trainees/Alice.Smith/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).updateTraineeTrainers(eq("Alice.Smith"), any(Set.class));
    }

    @Test
    void updateTrainers_EmptyListReturns400() throws Exception {
        TraineeTrainersUpdateRequest request = new TraineeTrainersUpdateRequest();
        request.setTrainerUsernames(List.of());

        mockMvc.perform(put("/api/trainees/Alice.Smith/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Trainings list

    @Test
    void getTrainings_UsesAuthenticatedUsername() throws Exception {
        when(facade.getTraineeTrainings("Alice.Smith", null, null, null, null))
                .thenReturn(List.of(new TrainingResponse()));

        mockMvc.perform(get("/api/trainees/Alice.Smith/trainings"))
                .andExpect(status().isOk());

        verify(facade).getTraineeTrainings("Alice.Smith", null, null, null, null);
    }

    @Test
    void getTrainings_WithFiltersReturns200() throws Exception {
        when(facade.getTraineeTrainings(eq("Alice.Smith"), any(), any(), eq("John"), eq("Yoga")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainees/Alice.Smith/trainings")
                        .param("periodFrom", "2024-01-01")
                        .param("periodTo", "2024-12-31")
                        .param("trainerUsername", "John")
                        .param("trainingType", "Yoga"))
                .andExpect(status().isOk());
    }

    // Helper

    private void setSecurityContext(String username) {
        User user = User.builder().username(username).password("pass").isActive(true).build();
        UserPrincipal principal = new UserPrincipal(
                user, List.of(new SimpleGrantedAuthority("ROLE_TRAINEE")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }
}
