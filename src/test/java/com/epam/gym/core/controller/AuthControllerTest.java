package com.epam.gym.core.controller;

import com.epam.gym.core.dto.request.ChangePasswordRequest;
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

import java.util.Base64;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private GymFacade facade;

    @InjectMocks
    private AuthController controller;

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
    void login_validTraineeCredentialsReturns200() throws Exception {
        when(facade.authenticateTrainee("alice", "pass")).thenReturn(true);

        mockMvc.perform(get("/api/auth/login")
                        .header("Authorization", basicAuth("alice", "pass")))
                .andExpect(status().isOk());
    }

    @Test
    void login_validTrainerCredentialsReturns200() throws Exception {
        when(facade.authenticateTrainee("john", "pass")).thenReturn(false);
        when(facade.authenticateTrainer("john", "pass")).thenReturn(true);

        mockMvc.perform(get("/api/auth/login")
                        .header("Authorization", basicAuth("john", "pass")))
                .andExpect(status().isOk());
    }

    @Test
    void login_invalidCredentialsReturns401() throws Exception {
        when(facade.authenticateTrainee("nobody", "wrong")).thenReturn(false);
        when(facade.authenticateTrainer("nobody", "wrong")).thenReturn(false);

        mockMvc.perform(get("/api/auth/login")
                        .header("Authorization", basicAuth("nobody", "wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication failed"));
    }

    @Test
    void login_missingAuthHeaderReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_validTraineeRequestReturns200() throws Exception {
        when(facade.authenticateTrainee("alice", "oldPass")).thenReturn(true);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("alice");
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass123");

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).changePassword("alice", "newPass123");
    }

    @Test
    void changePassword_validTrainerRequestReturns200() throws Exception {
        when(facade.authenticateTrainee("john", "oldPass")).thenReturn(false);
        when(facade.authenticateTrainer("john", "oldPass")).thenReturn(true);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("john");
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass123");

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).changePassword("john", "newPass123");
    }

    @Test
    void changePassword_wrongOldPasswordReturns401() throws Exception {
        when(facade.authenticateTrainee("alice", "wrongPass")).thenReturn(false);
        when(facade.authenticateTrainer("alice", "wrongPass")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("alice");
        request.setOldPassword("wrongPass");
        request.setNewPassword("newPass123");

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(facade, never()).changePassword("alice", "newPass123");
    }

    @Test
    void changePassword_blankUsernameReturns400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("");
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass123");

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
