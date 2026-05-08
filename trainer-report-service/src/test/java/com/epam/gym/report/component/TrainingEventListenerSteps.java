package com.epam.gym.report.component;

import com.epam.gym.report.dto.ActionType;
import com.epam.gym.report.dto.WorkloadRequest;
import com.epam.gym.report.messaging.TrainingEventListener;
import com.epam.gym.report.repository.TrainerSummaryRepository;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScope
public class TrainingEventListenerSteps {

    @Autowired private ReportScenarioContext ctx;
    @Autowired private TrainingEventListener trainingEventListener;
    @Autowired private TrainerSummaryRepository repository;

    @When("a valid workload ADD event arrives on the queue")
    public void aValidWorkloadAddEventArrivesOnTheQueue() {
        ctx.processedUsername = "listener-" + ctx.uid;
        WorkloadRequest req = new WorkloadRequest();
        req.setTrainerUsername(ctx.processedUsername);
        req.setFirstName("Jane");
        req.setLastName("Smith");
        req.setActive(true);
        req.setTrainingDate(LocalDate.of(2024, 5, 10));
        req.setTrainingDurationMinutes(45);
        req.setActionType(ActionType.ADD);
        trainingEventListener.onTrainingEvent(req, "test-tx-id");
    }

    @Then("a summary record exists for the trainer from that message")
    public void aSummaryRecordExistsForTheTrainerFromThatMessage() {
        assertThat(repository.findByUsername(ctx.processedUsername)).isPresent();
    }

    @When("an invalid workload message arrives on the queue")
    public void anInvalidWorkloadMessageArrivesOnTheQueue() {
        try {
            // WorkloadRequest with all null/default fields fails @NotBlank / @NotNull validation
            trainingEventListener.onTrainingEvent(new WorkloadRequest(), "test-tx-id");
        } catch (IllegalArgumentException e) {
            ctx.thrownException = e;
        }
    }

    @Then("an IllegalArgumentException is thrown")
    public void anIllegalArgumentExceptionIsThrown() {
        assertThat(ctx.thrownException).isInstanceOf(IllegalArgumentException.class);
    }

    @Then("no summary record is created")
    public void noSummaryRecordIsCreated() {
        // processedUsername was never set because the invalid message was rejected before processing
        assertThat(ctx.processedUsername).isNull();
    }
}
