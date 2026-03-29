package com.epam.gym.core.controller;

import com.epam.gym.core.dto.request.ChangePasswordRequest;
import com.epam.gym.core.dto.request.LoginRequest;
import com.epam.gym.core.exception.handler.RestExceptionHandler;
import com.epam.gym.core.model.User;
import com.epam.gym.core.model.UserPrincipal;
import com.epam.gym.core.service.JwtService;
import com.epam.gym.core.service.LoginAttemptService;
import com.epam.gym.core.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new RestExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // Login

    @Test
    void login_ValidCredentialsReturnsTokenWith200() throws Exception {
        UserPrincipal principal = principalForUser("alice");
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(principal)).thenReturn("jwt-token");

        LoginRequest request = loginRequest("alice", "pass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"));

        verify(loginAttemptService).recordSuccess("alice");
    }

    @Test
    void login_BadCredentialsReturns401AndRecordsFailure() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        LoginRequest request = loginRequest("alice", "wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(loginAttemptService).recordFailure("alice");
    }

    @Test
    void login_LockedAccountReturns401WithoutRecordingFailure() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new LockedException("locked"));

        LoginRequest request = loginRequest("alice", "pass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(loginAttemptService, never()).recordFailure(any());
    }

    @Test
    void login_BlankUsernameReturns400() throws Exception {
        LoginRequest request = loginRequest("", "pass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_BlankPasswordReturns400() throws Exception {
        LoginRequest request = loginRequest("alice", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Logout

    @Test
    void logout_AuthenticatedUserCallsRecordLogout() throws Exception {
        UserPrincipal principal = principalForUser("alice");
        setSecurityContext(principal);

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());

        verify(userService).recordLogout("alice");
    }

    // Change password

    @Test
    void changePassword_ValidRequestReturns200() throws Exception {
        UserPrincipal principal = principalForUser("alice");
        setSecurityContext(principal);

        ChangePasswordRequest request = changePasswordRequest("oldPass", "newPass");

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).changePassword("alice", "oldPass", "newPass");
    }

    @Test
    void changePassword_WrongOldPasswordReturns401() throws Exception {
        UserPrincipal principal = principalForUser("alice");
        setSecurityContext(principal);
        doThrow(new SecurityException("Invalid current password"))
                .when(userService).changePassword(any(), any(), any());

        ChangePasswordRequest request = changePasswordRequest("wrong", "newPass");

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_BlankOldPasswordReturns400() throws Exception {
        UserPrincipal principal = principalForUser("alice");
        setSecurityContext(principal);

        ChangePasswordRequest request = changePasswordRequest("", "newPass");

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Helpers

    private UserPrincipal principalForUser(String username) {
        User user = User.builder().username(username).password("pass").isActive(true).build();
        return new UserPrincipal(user, List.of(new SimpleGrantedAuthority("ROLE_TRAINEE")));
    }

    private void setSecurityContext(UserPrincipal principal) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        return req;
    }

    private ChangePasswordRequest changePasswordRequest(String oldPassword, String newPassword) {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword(oldPassword);
        req.setNewPassword(newPassword);
        return req;
    }
}
