package com.epam.gym.report.component;

import io.cucumber.java.Before;
import io.cucumber.spring.ScenarioScope;

import java.util.UUID;

@ScenarioScope
public class ReportScenarioContext {
    public String uid;
    public String processedUsername;
    public int firstDuration;
    public int secondDuration;
    public Exception thrownException;

    @Before
    public void init() {
        uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        processedUsername = null;
        firstDuration = 0;
        secondDuration = 0;
        thrownException = null;
    }
}
