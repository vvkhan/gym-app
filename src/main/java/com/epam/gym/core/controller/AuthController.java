package com.epam.gym.core.controller;

import com.epam.gym.core.dto.request.ChangePasswordRequest;
import com.epam.gym.core.facade.GymFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and password management")
public class AuthController {

    private final GymFacade facade;
    private final AuthenticationHelper authHelper;

    public AuthController(GymFacade facade, AuthenticationHelper authHelper) {
        this.facade = facade;
        this.authHelper = authHelper;
    }

    @GetMapping("/login")
    @Operation(summary = "Login", description = "Authenticate as a trainee or trainer using Basic Auth header")
    @ApiResponse(responseCode = "200", description = "Authenticated successfully")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<Void> login(HttpServletRequest request) {
        authHelper.authenticateAny(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Change password for a trainee or trainer")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Invalid current password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        boolean authenticated = facade.authenticateUser(request.getUsername(), request.getOldPassword());
        if (!authenticated) {
            throw new SecurityException("Invalid username or password");
        }
        facade.changePassword(request.getUsername(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
