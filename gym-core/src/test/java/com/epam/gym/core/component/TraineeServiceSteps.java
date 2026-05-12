package com.epam.gym.core.component;

import com.epam.gym.core.model.Trainee;
import com.epam.gym.core.repository.TrainingTypeRepository;
import com.epam.gym.core.service.TraineeService;
import com.epam.gym.core.service.TrainerService;
import com.epam.gym.core.service.TrainingService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScope
public class TraineeServiceSteps {

    @Autowired private ScenarioContext ctx;
    @Autowired private TraineeService traineeService;
    @Autowired private TrainerService trainerService;
    @Autowired private TrainingService trainingService;
    @Autowired private TrainingTypeRepository trainingTypeRepository;

    private static final LocalDate FILTER_DATE = LocalDate.now();

    // ----- Registration -----

    @When("two trainees are registered with identical first and last names")
    public void twoTraineesAreRegisteredWithIdenticalNames() {
        ctx.trainee  = traineeService.createTrainee("Same" + ctx.uid, "Name" + ctx.uid, LocalDate.of(1990, 1, 1), null);
        ctx.trainee2 = traineeService.createTrainee("Same" + ctx.uid, "Name" + ctx.uid, LocalDate.of(1991, 1, 1), null);
    }

    @Then("the returned username is not blank")
    public void theReturnedUsernameIsNotBlank() {
        assertThat(ctx.trainee.getUser().getUsername()).isNotBlank();
    }

    @Then("the raw password is a 10-character alphanumeric string")
    public void theRawPasswordIs10CharAlphanumeric() {
        assertThat(ctx.trainee.getUser().getRawPassword()).hasSize(10).matches("[A-Za-z0-9]+");
    }

    @Then("the trainee is active by default")
    public void theTraineeIsActiveByDefault() {
        assertThat(ctx.trainee.getUser().getIsActive()).isTrue();
    }

    @Then("their usernames are different")
    public void theirUsernamesAreDifferent() {
        assertThat(ctx.trainee.getUser().getUsername())
                .isNotEqualTo(ctx.trainee2.getUser().getUsername());
    }

    // ----- Retrieve -----

    @When("I look up the trainee by their generated username")
    public void iLookUpTheTraineeByTheirGeneratedUsername() {
        var result = traineeService.getTraineeByUsername(ctx.trainee.getUser().getUsername());
        ctx.optionalWasEmpty = result.isEmpty();
        result.ifPresent(t -> ctx.trainee = t);
    }

    @Then("the returned profile has the same first and last name as the registered trainee")
    public void theReturnedProfileHasSameFirstAndLastNameTrainee() {
        assertThat(ctx.trainee.getUser().getFirstName()).startsWith("First");
        assertThat(ctx.trainee.getUser().getLastName()).startsWith("Last");
    }

    @When("I look up a trainee by a username that does not exist")
    public void iLookUpATraineeByAUsernameThatDoesNotExist() {
        var result = traineeService.getTraineeByUsername("nonexistent-" + ctx.uid);
        ctx.optionalWasEmpty = result.isEmpty();
    }

    // ----- Update -----

    @When("the trainee address and date of birth are updated")
    public void theTraineeAddressAndDateOfBirthAreUpdated() {
        ctx.trainee = traineeService.updateTrainee(
                ctx.trainee.getUser().getUsername(),
                ctx.trainee.getUser().getFirstName(),
                ctx.trainee.getUser().getLastName(),
                LocalDate.of(1995, 6, 15),
                "Updated Address " + ctx.uid,
                true);
    }

    @Then("the stored address matches the updated value")
    public void theStoredAddressMatchesTheUpdatedValue() {
        assertThat(ctx.trainee.getAddress()).contains("Updated Address");
    }

    @When("I update a trainee that does not exist")
    public void iUpdateATraineeThatDoesNotExist() {
        try {
            traineeService.updateTrainee("nonexistent-" + ctx.uid, "A", "B", null, null, true);
        } catch (NoSuchElementException e) {
            ctx.thrownException = e;
        }
    }

    // ----- Activate / Deactivate -----

    @And("the trainee is deactivated")
    public void theTraineeIsDeactivated() {
        traineeService.deactivate(ctx.trainee.getUser().getUsername());
    }

    @When("the trainee is activated")
    public void theTraineeIsActivated() {
        traineeService.activate(ctx.trainee.getUser().getUsername());
    }

    @When("the trainee is activated without prior deactivation")
    public void theTraineeIsActivatedWithoutPriorDeactivation() {
        try {
            traineeService.activate(ctx.trainee.getUser().getUsername());
        } catch (IllegalStateException e) {
            ctx.thrownException = e;
        }
    }

    @When("the trainee is deactivated again")
    public void theTraineeIsDeactivatedAgain() {
        try {
            traineeService.deactivate(ctx.trainee.getUser().getUsername());
        } catch (IllegalStateException e) {
            ctx.thrownException = e;
        }
    }

    @Then("the trainee is active")
    public void theTraineeIsActive() {
        Trainee reloaded = traineeService.getTraineeByUsername(ctx.trainee.getUser().getUsername()).orElseThrow();
        assertThat(reloaded.getUser().getIsActive()).isTrue();
    }

    @Then("the trainee is inactive")
    public void theTraineeIsInactive() {
        Trainee reloaded = traineeService.getTraineeByUsername(ctx.trainee.getUser().getUsername()).orElseThrow();
        assertThat(reloaded.getUser().getIsActive()).isFalse();
    }

    // ----- Delete -----

    @When("I delete a trainee that does not exist")
    public void iDeleteATraineeThatDoesNotExist() {
        try {
            traineeService.deleteTrainee("nonexistent-" + ctx.uid);
        } catch (NoSuchElementException e) {
            ctx.thrownException = e;
        }
    }

    @When("the trainee is deleted")
    public void theTraineeIsDeleted() {
        ctx.deletedTraineeUsername = ctx.trainee.getUser().getUsername();
        traineeService.deleteTrainee(ctx.deletedTraineeUsername);
        ctx.trainee = null;
    }

    @Then("looking up the trainee by their username returns empty")
    public void lookingUpTheTraineeByTheirUsernameReturnsEmpty() {
        assertThat(traineeService.getTraineeByUsername(ctx.deletedTraineeUsername)).isEmpty();
    }

    // ----- Trainer assignment -----

    @And("the trainer is assigned to the trainee")
    public void theTrainerIsAssignedToTheTrainee() {
        traineeService.updateTrainers(
                ctx.trainee.getUser().getUsername(),
                Set.of(ctx.trainer.getUser().getUsername()));
    }

    @When("not-assigned trainers are requested for the trainee")
    public void notAssignedTrainersAreRequestedForTheTrainee() {
        ctx.notAssignedTrainers = traineeService.getNotAssignedTrainers(ctx.trainee.getUser().getUsername());
    }

    @Then("the list does not include the assigned trainer")
    public void theListDoesNotIncludeTheAssignedTrainer() {
        String assignedUsername = ctx.trainer.getUser().getUsername();
        assertThat(ctx.notAssignedTrainers)
                .noneMatch(t -> t.getUser().getUsername().equals(assignedUsername));
    }

    // ----- Training date filter -----

    @And("a training is created before the filter date")
    public void aTrainingIsCreatedBeforeTheFilterDate() {
        var type = trainingTypeRepository.findAll().get(0);
        trainingService.createTraining(ctx.trainee, ctx.trainer,
                "Before-" + ctx.uid, type, FILTER_DATE.minusDays(1), 30);
    }

    @And("another training is created after the filter date")
    public void anotherTrainingIsCreatedAfterTheFilterDate() {
        var type = trainingTypeRepository.findAll().get(0);
        trainingService.createTraining(ctx.trainee, ctx.trainer,
                "After-" + ctx.uid, type, FILTER_DATE.plusDays(1), 30);
    }

    @When("trainings are requested for the trainee with a from-date filter")
    public void trainingsAreRequestedWithFromDateFilter() {
        ctx.filteredTrainings = traineeService.getTrainings(
                ctx.trainee.getUser().getUsername(), FILTER_DATE, null, null, null);
    }

    @Then("only the training created after the filter date is returned")
    public void onlyTheTrainingCreatedAfterTheFilterDateIsReturned() {
        assertThat(ctx.filteredTrainings).hasSize(1);
        assertThat(ctx.filteredTrainings.get(0).getTrainingName()).startsWith("After-");
    }
}
