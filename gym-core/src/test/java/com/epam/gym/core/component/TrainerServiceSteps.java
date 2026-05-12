package com.epam.gym.core.component;

import com.epam.gym.core.model.Trainer;
import com.epam.gym.core.repository.TrainingTypeRepository;
import com.epam.gym.core.service.TrainerService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioScope
public class TrainerServiceSteps {

    @Autowired private ScenarioContext ctx;
    @Autowired private TrainerService trainerService;
    @Autowired private TrainingTypeRepository trainingTypeRepository;

    @Given("a training type exists in the system")
    public void aTrainingTypeExistsInTheSystem() {
        // Training types are seeded via data.sql; this step documents the prerequisite
        assertThat(trainingTypeRepository.findAll()).isNotEmpty();
    }

    // ----- Registration -----

    @Then("the returned trainer username is not blank")
    public void theReturnedTrainerUsernameIsNotBlank() {
        assertThat(ctx.trainer.getUser().getUsername()).isNotBlank();
    }

    @Then("the trainer raw password is a 10-character alphanumeric string")
    public void theTrainerRawPasswordIs10CharAlphanumeric() {
        assertThat(ctx.trainer.getUser().getRawPassword()).hasSize(10).matches("[A-Za-z0-9]+");
    }

    @When("a trainer is registered with an unknown specialization UUID")
    public void aTrainerIsRegisteredWithUnknownSpecializationUUID() {
        try {
            trainerService.createTrainer("TFirst" + ctx.uid, "TLast" + ctx.uid, UUID.randomUUID());
        } catch (NoSuchElementException e) {
            ctx.thrownException = e;
        }
    }

    // ----- Retrieve -----

    @When("I look up the trainer by their generated username")
    public void iLookUpTheTrainerByTheirGeneratedUsername() {
        var result = trainerService.getTrainerByUsername(ctx.trainer.getUser().getUsername());
        ctx.optionalWasEmpty = result.isEmpty();
        result.ifPresent(t -> ctx.trainer = t);
    }

    @Then("the returned profile has the same first and last name as the registered trainer")
    public void theReturnedProfileHasSameFirstAndLastNameTrainer() {
        assertThat(ctx.trainer.getUser().getFirstName()).startsWith("TFirst");
        assertThat(ctx.trainer.getUser().getLastName()).startsWith("TLast");
    }

    @When("I look up a trainer by a username that does not exist")
    public void iLookUpATrainerByAUsernameThatDoesNotExist() {
        var result = trainerService.getTrainerByUsername("nonexistent-" + ctx.uid);
        ctx.optionalWasEmpty = result.isEmpty();
    }

    // ----- Update -----

    @When("the trainer first and last name are updated")
    public void theTrainerFirstAndLastNameAreUpdated() {
        UUID typeId = ctx.trainer.getSpecialization().getId();
        ctx.trainer = trainerService.updateTrainer(
                ctx.trainer.getUser().getUsername(),
                "Updated" + ctx.uid, "Name" + ctx.uid,
                typeId, true);
    }

    @Then("the stored trainer reflects the updated names")
    public void theStoredTrainerReflectsTheUpdatedNames() {
        Trainer reloaded = trainerService.getTrainerByUsername(ctx.trainer.getUser().getUsername()).orElseThrow();
        assertThat(reloaded.getUser().getFirstName()).startsWith("Updated");
    }

    @When("I update a trainer that does not exist")
    public void iUpdateATrainerThatDoesNotExist() {
        try {
            UUID typeId = trainingTypeRepository.findAll().get(0).getId();
            trainerService.updateTrainer("nonexistent-" + ctx.uid, "A", "B", typeId, true);
        } catch (NoSuchElementException e) {
            ctx.thrownException = e;
        }
    }

    // ----- Activate / Deactivate -----

    @When("the trainer is activated")
    public void theTrainerIsActivated() {
        trainerService.activate(ctx.trainer.getUser().getUsername());
    }

    @When("the trainer is deactivated")
    public void theTrainerIsDeactivated() {
        trainerService.deactivate(ctx.trainer.getUser().getUsername());
    }

    @When("the trainer is activated without prior deactivation")
    public void theTrainerIsActivatedWithoutPriorDeactivation() {
        try {
            trainerService.activate(ctx.trainer.getUser().getUsername());
        } catch (IllegalStateException e) {
            ctx.thrownException = e;
        }
    }

    @When("the trainer is deactivated again")
    public void theTrainerIsDeactivatedAgain() {
        try {
            trainerService.deactivate(ctx.trainer.getUser().getUsername());
        } catch (IllegalStateException e) {
            ctx.thrownException = e;
        }
    }

    @Then("the trainer is active")
    public void theTrainerIsActive() {
        Trainer reloaded = trainerService.getTrainerByUsername(ctx.trainer.getUser().getUsername()).orElseThrow();
        assertThat(reloaded.getUser().getIsActive()).isTrue();
    }

    @Then("the trainer is inactive")
    public void theTrainerIsInactive() {
        Trainer reloaded = trainerService.getTrainerByUsername(ctx.trainer.getUser().getUsername()).orElseThrow();
        assertThat(reloaded.getUser().getIsActive()).isFalse();
    }
}
