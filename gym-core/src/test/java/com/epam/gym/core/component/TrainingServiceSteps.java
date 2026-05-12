package com.epam.gym.core.component;

import com.epam.gym.core.repository.TrainingTypeRepository;
import com.epam.gym.core.service.TraineeService;
import com.epam.gym.core.service.TrainerService;
import com.epam.gym.core.service.TrainingService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScope
public class TrainingServiceSteps {

    @Autowired private ScenarioContext ctx;
    @Autowired private TraineeService traineeService;
    @Autowired private TrainerService trainerService;
    @Autowired private TrainingService trainingService;
    @Autowired private TrainingTypeRepository trainingTypeRepository;

    @Given("a trainee and a trainer exist")
    public void aTraineeAndATrainerExist() {
        ctx.trainee = traineeService.createTrainee(
                "First" + ctx.uid, "Last" + ctx.uid,
                LocalDate.of(1990, 1, 1), null);
        UUID typeId = trainingTypeRepository.findAll().get(0).getId();
        ctx.trainer = trainerService.createTrainer(
                "TFirst" + ctx.uid, "TLast" + ctx.uid, typeId);
    }

    @When("a training session is created")
    public void aTrainingSessionIsCreated() {
        var type = trainingTypeRepository.findAll().get(0);
        ctx.createdTraining = trainingService.createTraining(
                ctx.trainee, ctx.trainer,
                "Session-" + ctx.uid, type,
                LocalDate.now(), 60);
    }

    @Then("the training is persisted with the correct name and duration")
    public void theTrainingIsPersistedWithCorrectNameAndDuration() {
        assertThat(ctx.createdTraining.getId()).isNotNull();
        assertThat(ctx.createdTraining.getTrainingName()).startsWith("Session-");
        assertThat(ctx.createdTraining.getDuration()).isEqualTo(60);
    }

    @When("all training types are retrieved")
    public void allTrainingTypesAreRetrieved() {
        ctx.allTrainingTypes = trainingService.getAllTrainingTypes();
    }

    @Then("the list is not empty")
    public void theListIsNotEmpty() {
        assertThat(ctx.allTrainingTypes).isNotEmpty();
    }

    @Then("every entry has a non-blank name")
    public void everyEntryHasANonBlankName() {
        assertThat(ctx.allTrainingTypes)
                .allMatch(t -> t.getTrainingTypeName() != null && !t.getTrainingTypeName().isBlank());
    }
}
