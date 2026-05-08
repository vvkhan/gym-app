package com.epam.gym.core.component;

import com.epam.gym.core.repository.TrainerRepository;
import com.epam.gym.core.repository.TrainingTypeRepository;
import com.epam.gym.core.service.TraineeService;
import com.epam.gym.core.service.TrainerService;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScope
public class SharedComponentSteps {

    @Autowired private ScenarioContext ctx;
    @Autowired private TraineeService traineeService;
    @Autowired private TrainerService trainerService;
    @Autowired private TrainingTypeRepository trainingTypeRepository;
    @Autowired private TrainerRepository trainerRepository;

    @After
    public void tearDown() {
        if (ctx.trainee != null) {
            try {
                traineeService.deleteTrainee(ctx.trainee.getUser().getUsername());
            } catch (Exception ignored) {}
        }
        if (ctx.trainee2 != null) {
            try {
                traineeService.deleteTrainee(ctx.trainee2.getUser().getUsername());
            } catch (Exception ignored) {}
        }
        if (ctx.trainer != null) {
            trainerRepository.findByUserUsername(ctx.trainer.getUser().getUsername())
                    .ifPresent(trainerRepository::delete);
        }
    }

    @Given("a trainee is registered")
    public void aTraineeIsRegistered() {
        ctx.trainee = traineeService.createTrainee(
                "First" + ctx.uid, "Last" + ctx.uid,
                LocalDate.of(1990, 1, 1), "Address " + ctx.uid);
    }

    @Given("a trainer is registered")
    public void aTrainerIsRegistered() {
        UUID typeId = trainingTypeRepository.findAll().get(0).getId();
        ctx.trainer = trainerService.createTrainer(
                "TFirst" + ctx.uid, "TLast" + ctx.uid, typeId);
    }

    @Then("the returned Optional is empty")
    public void theReturnedOptionalIsEmpty() {
        assertThat(ctx.optionalWasEmpty).isTrue();
    }

    @Then("a NoSuchElementException is thrown")
    public void aNoSuchElementExceptionIsThrown() {
        assertThat(ctx.thrownException).isInstanceOf(NoSuchElementException.class);
    }

    @Then("an IllegalStateException is thrown")
    public void anIllegalStateExceptionIsThrown() {
        assertThat(ctx.thrownException).isInstanceOf(IllegalStateException.class);
    }
}
