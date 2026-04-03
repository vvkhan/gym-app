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
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new RestExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(validator)
                .build();
        setSecurityContext("John.Smith");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // Registration (public — no principal needed)

    @Test
    void register_ValidRequestReturns201() throws Exception {
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
    void register_MissingSpecializationReturns400() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Smith");

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_SpecializationNotFoundReturns404() throws Exception {
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
    void getProfile_UsesAuthenticatedUsername() throws Exception {
        when(facade.getTrainerByUsername("John.Smith")).thenReturn(new TrainerProfileResponse());

        mockMvc.perform(get("/api/trainers/John.Smith"))
                .andExpect(status().isOk());

        verify(facade).getTrainerByUsername("John.Smith");
    }

    @Test
    void getProfile_TrainerNotFoundReturns404() throws Exception {
        when(facade.getTrainerByUsername("John.Smith"))
                .thenThrow(new NoSuchElementException("Trainer not found: John.Smith"));

        mockMvc.perform(get("/api/trainers/John.Smith"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Trainer not found: John.Smith"));
    }

    // Update profile

    @Test
    void updateProfile_UsesAuthenticatedUsername() throws Exception {
        when(facade.updateTrainerProfile(eq("John.Smith"), any(), any(), any()))
                .thenReturn(new UpdatedTrainerProfileResponse());

        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setIsActive(true);

        mockMvc.perform(put("/api/trainers/John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).updateTrainerProfile(eq("John.Smith"), any(), any(), any());
    }

    @Test
    void updateProfile_MissingFirstNameReturns400() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setLastName("Smith");
        request.setIsActive(true);

        mockMvc.perform(put("/api/trainers/John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Activate / Deactivate

    @Test
    void activate_IsActiveTrueCallsActivate() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setIsActive(true);

        mockMvc.perform(patch("/api/trainers/John.Smith/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).activateTrainer("John.Smith");
    }

    @Test
    void activate_IsActiveFalseCallsDeactivate() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setIsActive(false);

        mockMvc.perform(patch("/api/trainers/John.Smith/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).deactivateTrainer("John.Smith");
    }

    @Test
    void activate_AlreadyInactiveReturns409() throws Exception {
        doThrow(new IllegalStateException("Trainer is already inactive: John.Smith"))
                .when(facade).deactivateTrainer("John.Smith");

        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setIsActive(false);

        mockMvc.perform(patch("/api/trainers/John.Smith/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // Trainings list

    @Test
    void getTrainings_UsesAuthenticatedUsername() throws Exception {
        when(facade.getTrainerTrainings("John.Smith", null, null, null))
                .thenReturn(List.of(new TrainingResponse()));

        mockMvc.perform(get("/api/trainers/John.Smith/trainings"))
                .andExpect(status().isOk());

        verify(facade).getTrainerTrainings("John.Smith", null, null, null);
    }

    @Test
    void getTrainings_WithTraineeNameReturns200() throws Exception {
        when(facade.getTrainerTrainings(eq("John.Smith"), any(), any(), eq("Alice")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainers/John.Smith/trainings")
                        .param("traineeUsername", "Alice"))
                .andExpect(status().isOk());
    }

    // Helper

    private void setSecurityContext(String username) {
        User user = User.builder().username(username).password("pass").isActive(true).build();
        UserPrincipal principal = new UserPrincipal(
                user, List.of(new SimpleGrantedAuthority("ROLE_TRAINER")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }
}
