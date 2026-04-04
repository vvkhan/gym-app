package com.epam.gym.core.controller;

import com.epam.gym.core.dto.request.ChangePasswordRequest;
import com.epam.gym.core.dto.request.LoginRequest;
import com.epam.gym.core.dto.response.LoginResponse;
import com.epam.gym.core.model.UserPrincipal;
import com.epam.gym.core.service.JwtService;
import com.epam.gym.core.service.LoginAttemptService;
import com.epam.gym.core.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login, logout and password management")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          LoginAttemptService loginAttemptService,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate and receive a JWT access token")
    @ApiResponse(responseCode = "200", description = "Authenticated successfully")
    @ApiResponse(responseCode = "401", description = "Invalid credentials or account locked")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            loginAttemptService.recordSuccess(request.getUsername());
            return ResponseEntity.ok(new LoginResponse(jwtService.generateToken(principal)));
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (BadCredentialsException e) {
            loginAttemptService.recordFailure(request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate the current JWT token")
    @ApiResponse(responseCode = "200", description = "Logged out successfully")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal principal) {
        userService.recordLogout(principal.getUsername());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @Operation(summary = "Change password")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Invalid current password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                               @AuthenticationPrincipal UserPrincipal principal) {
        userService.changePassword(principal.getUsername(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
