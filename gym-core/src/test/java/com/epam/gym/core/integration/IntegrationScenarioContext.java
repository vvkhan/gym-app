package com.epam.gym.core.integration;

import io.cucumber.java.Before;
import io.cucumber.spring.ScenarioScope;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@ScenarioScope
public class IntegrationScenarioContext {
    public String uid;

    public String traineeUsername;
    public String traineePassword;
    public String traineeToken;

    public String trainerUsername;
    public String trainerPassword;
    public String trainerToken;

    public String activeToken;
    public ResponseEntity<String> lastResponse;

    @Before
    public void init() {
        uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        traineeUsername = null;
        traineePassword = null;
        traineeToken = null;
        trainerUsername = null;
        trainerPassword = null;
        trainerToken = null;
        activeToken = null;
        lastResponse = null;
    }
}
