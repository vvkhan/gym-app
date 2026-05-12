package com.epam.gym.report.component;

import com.epam.gym.report.dto.ActionType;
import com.epam.gym.report.dto.WorkloadRequest;
import com.epam.gym.report.model.TrainerSummary;
import com.epam.gym.report.repository.TrainerSummaryRepository;
import com.epam.gym.report.service.ReportService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScope
public class ReportServiceSteps {

    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 3, 1);

    @Autowired private ReportScenarioContext ctx;
    @Autowired private ReportService reportService;
    @Autowired private TrainerSummaryRepository repository;

    // ----- ADD event -----

    @When("a workload ADD event is processed for a trainer")
    public void aWorkloadAddEventIsProcessedForATrainer() {
        ctx.firstDuration = 60;
        ctx.processedUsername = "trainer-" + ctx.uid;
        reportService.processWorkload(buildRequest(ctx.processedUsername, ctx.firstDuration, ActionType.ADD));
    }

    @Given("a workload ADD event has been processed for a trainer")
    public void givenAWorkloadAddEventHasBeenProcessedForATrainer() {
        ctx.firstDuration = 60;
        ctx.processedUsername = "trainer-" + ctx.uid;
        reportService.processWorkload(buildRequest(ctx.processedUsername, ctx.firstDuration, ActionType.ADD));
    }

    @Given("a workload ADD event has been processed for a trainer with a small duration")
    public void givenAWorkloadAddEventHasBeenProcessedWithSmallDuration() {
        ctx.firstDuration = 10;
        ctx.processedUsername = "trainer-" + ctx.uid;
        reportService.processWorkload(buildRequest(ctx.processedUsername, ctx.firstDuration, ActionType.ADD));
    }

    @Then("a summary record exists for that trainer")
    public void aSummaryRecordExistsForThatTrainer() {
        assertThat(repository.findByUsername(ctx.processedUsername)).isPresent();
    }

    @Then("the duration for that training month equals the submitted duration")
    public void theDurationForThatTrainingMonthEqualsTheSubmittedDuration() {
        int stored = getDurationForMonth(ctx.processedUsername, TRAINING_DATE.getYear(), TRAINING_DATE.getMonthValue());
        assertThat(stored).isEqualTo(ctx.firstDuration);
    }

    // ----- Accumulation -----

    @When("a second ADD event for the same trainer and month is processed")
    public void aSecondAddEventForTheSameTrainerAndMonthIsProcessed() {
        ctx.secondDuration = 45;
        reportService.processWorkload(buildRequest(ctx.processedUsername, ctx.secondDuration, ActionType.ADD));
    }

    @Then("the stored duration for that month equals the sum of both submitted durations")
    public void theStoredDurationEqualsTheSumOfBothDurations() {
        int stored = getDurationForMonth(ctx.processedUsername, TRAINING_DATE.getYear(), TRAINING_DATE.getMonthValue());
        assertThat(stored).isEqualTo(ctx.firstDuration + ctx.secondDuration);
    }

    // ----- DELETE event -----

    @When("a DELETE event for the same trainer, month, and duration is processed")
    public void aDeleteEventForTheSameTrainerMonthAndDurationIsProcessed() {
        reportService.processWorkload(buildRequest(ctx.processedUsername, ctx.firstDuration, ActionType.DELETE));
    }

    @When("a DELETE event with a larger duration is processed for the same month")
    public void aDeleteEventWithALargerDurationIsProcessedForTheSameMonth() {
        reportService.processWorkload(buildRequest(ctx.processedUsername, ctx.firstDuration + 100, ActionType.DELETE));
    }

    @Then("the stored duration for that month is zero")
    public void theStoredDurationForThatMonthIsZero() {
        int stored = getDurationForMonth(ctx.processedUsername, TRAINING_DATE.getYear(), TRAINING_DATE.getMonthValue());
        assertThat(stored).isZero();
    }

    // ----- Metadata update -----

    @When("a workload ADD event for the same trainer is processed with a different name")
    public void aWorkloadAddEventForTheSameTrainerIsProcessedWithDifferentName() {
        WorkloadRequest req = buildRequest(ctx.processedUsername, 30, ActionType.ADD);
        req.setFirstName("Updated");
        req.setLastName("Name");
        reportService.processWorkload(req);
    }

    @Then("the stored summary reflects the updated name")
    public void theStoredSummaryReflectsTheUpdatedName() {
        TrainerSummary summary = repository.findByUsername(ctx.processedUsername).orElseThrow();
        assertThat(summary.getFirstName()).isEqualTo("Updated");
    }

    // ----- Not found -----

    @When("the summary is requested for a trainer username that does not exist")
    public void theSummaryIsRequestedForATrainerUsernameThatDoesNotExist() {
        try {
            reportService.getSummary("nonexistent-" + ctx.uid);
        } catch (NoSuchElementException e) {
            ctx.thrownException = e;
        }
    }

    @Then("a NoSuchElementException is thrown")
    public void aNoSuchElementExceptionIsThrown() {
        assertThat(ctx.thrownException).isInstanceOf(NoSuchElementException.class);
    }

    // ----- Helpers -----

    private WorkloadRequest buildRequest(String username, int durationMinutes, ActionType action) {
        WorkloadRequest req = new WorkloadRequest();
        req.setTrainerUsername(username);
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setActive(true);
        req.setTrainingDate(TRAINING_DATE);
        req.setTrainingDurationMinutes(durationMinutes);
        req.setActionType(action);
        return req;
    }

    private int getDurationForMonth(String username, int year, int month) {
        return repository.findByUsername(username)
                .flatMap(s -> s.getYears().stream()
                        .filter(y -> y.getYear() == year).findFirst())
                .flatMap(y -> y.getMonths().stream()
                        .filter(m -> m.getMonth() == month).findFirst())
                .map(TrainerSummary.MonthSummary::getTotalDurationMinutes)
                .orElse(0);
    }
}
