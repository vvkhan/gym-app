package com.epam.gym.report.component;

import com.epam.gym.report.repository.TrainerSummaryRepository;
import io.cucumber.java.After;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;

@ScenarioScope
public class SharedReportComponentSteps {

    @Autowired private ReportScenarioContext ctx;
    @Autowired private TrainerSummaryRepository repository;

    @After
    public void tearDown() {
        if (ctx.processedUsername != null) {
            repository.findByUsername(ctx.processedUsername).ifPresent(repository::delete);
        }
    }
}
